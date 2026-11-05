package br.com.spotmydeal.bo;

import br.com.spotmydeal.dao.LojaDao;
import br.com.spotmydeal.dao.MissaoDao;
import br.com.spotmydeal.dao.ResgateDao;
import br.com.spotmydeal.dao.UsuarioDao;
import br.com.spotmydeal.dao.UsuarioMissaoDao;
import br.com.spotmydeal.exception.ConflitoException;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.exception.ResgateBloqueadoException;
import br.com.spotmydeal.model.Loja;
import br.com.spotmydeal.model.MissaoSustentabilidade;
import br.com.spotmydeal.model.Resgate;
import br.com.spotmydeal.model.Usuario;
import br.com.spotmydeal.model.UsuarioMissao;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Regras de gamificação do SoulUp: cadastro, missões, resgate de cupons e ranking.



   - virou @Service e recebe os DAOs pelo construtor, em vez de guardar uma lista de
     usuários em memória. A lista era um espelho do banco que podia dessincronizar;
     agora a fonte da verdade é sempre o Oracle;
   - sumiu o "modo memória" (o construtor sem DAO). Numa API ele não faz sentido:
     dados que somem quando o servidor reinicia não servem para o front;
   - saiu todo o System.out. Antes, um erro de validação era impresso no console e o
     método retornava em silêncio — quem chamava não tinha como saber que falhou.
     Agora cada falha lança uma exceção, que o ApiExceptionHandler traduz num status
     HTTP que o front entende;
   - concluirMissao() e processarResgate() passaram a gravar nas tabelas de
     relacionamento. Era o que faltava na Sprint 3.

   O que foi preservado de propósito: o Bubble Sort do ranking e a validação de e-mail
   percorrendo os caracteres na mão, que são lógica própria da solução. */
@Service
public class GamificacaoBO {

    // Quantos dias o usuário tem, depois de concluir uma missão, para resgatar um cupom
    private static final int DIAS_LIMITE_RESGATE = 7;

    // Pontos concedidos por um resgate (valor herdado da Sprint 2)
    private static final int PONTOS_POR_RESGATE = 122;

    private final UsuarioDao usuarioDao;
    private final MissaoDao missaoDao;
    private final LojaDao lojaDao;
    private final UsuarioMissaoDao usuarioMissaoDao;
    private final ResgateDao resgateDao;
    private final GeolocalizacaoBO geolocalizacaoBO;

    // Injeção pelo construtor: com um único construtor o @Autowired é dispensável
    public GamificacaoBO(UsuarioDao usuarioDao,
                         MissaoDao missaoDao,
                         LojaDao lojaDao,
                         UsuarioMissaoDao usuarioMissaoDao,
                         ResgateDao resgateDao,
                         GeolocalizacaoBO geolocalizacaoBO) {
        this.usuarioDao = usuarioDao;
        this.missaoDao = missaoDao;
        this.lojaDao = lojaDao;
        this.usuarioMissaoDao = usuarioMissaoDao;
        this.resgateDao = resgateDao;
        this.geolocalizacaoBO = geolocalizacaoBO;
    }

    // ------------------------------------------------------------------
    // Usuários
    // ------------------------------------------------------------------

    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDao.listar();
    }

    public Usuario buscarUsuario(int id) throws SQLException, EntidadeNaoEncontradaException {
        return usuarioDao.buscar(id);
    }

    // Registra um novo usuário na plataforma (valida e grava no banco)
    public Usuario registrarUsuario(Usuario usuario)
            throws SQLException, RegraNegocioException {
        validarDados(usuario.getNome(), usuario.getEmail(), -1);

        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim());

        // O DAO devolve o código gerado pela sequence dentro do próprio objeto
        usuarioDao.cadastrar(usuario);
        return usuario;
    }

    /* Altera nome e e-mail do usuário, respeitando as mesmas regras do cadastro.

       A pontuação e o nível não entram aqui de propósito: quem mexe neles é a conclusão
       de missão e o resgate. Deixar o front enviar pontuação direto abriria caminho
       para alguém subir de nível sem fazer missão nenhuma. */
    public Usuario atualizarUsuario(int id, String novoNome, String novoEmail)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Usuario usuario = usuarioDao.buscar(id);
        validarDados(novoNome, novoEmail, id);

        usuario.setNome(novoNome.trim());
        usuario.setEmail(novoEmail.trim());
        usuarioDao.atualizar(usuario);
        return usuario;
    }

    /* Remove o usuário, desde que ele não tenha histórico.

       Mesma guarda que a LojaBO e a MissaoBO já faziam: conclusão de missão e resgate
       de cupom apontam para o usuário por chave estrangeira. Sem a checagem aqui, quem
       barra é o banco, e a falha chega ao cliente como 500 — quando na verdade é regra
       de negócio, que merece 400 e uma explicação do que fazer. */
    public void removerUsuario(int id)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Usuario usuario = usuarioDao.buscar(id);

        if (!usuarioMissaoDao.listarPorUsuario(id).isEmpty()) {
            throw new RegraNegocioException("O usuario " + usuario.getNome()
                    + " possui missoes concluidas e nao pode ser excluido."
                    + " Apagar levaria junto o historico de pontuacao.");
        }

        if (!resgateDao.listarPorUsuario(id).isEmpty()) {
            throw new RegraNegocioException("O usuario " + usuario.getNome()
                    + " possui cupons resgatados e nao pode ser excluido."
                    + " Apagar levaria junto o historico de resgates.");
        }

        usuarioDao.apagar(id);
    }

    /* Método de negócio: valida os dados antes de cadastrar/alterar um usuário.
    Regras:
    -Nome com pelo menos 3 letras
    -E-mail com formato mínimo (texto@texto.texto)
    -E-mail único na plataforma (ignora o próprio usuário quando é uma alteração)
     */
    public void validarDados(String nome, String email, int idIgnorado)
            throws SQLException, RegraNegocioException {
        if (nome == null || nome.trim().length() < 3) {
            throw new RegraNegocioException("Nome invalido: informe pelo menos 3 caracteres.");
        }
        if (!emailValido(email)) {
            throw new RegraNegocioException("E-mail invalido: use o formato nome@dominio.com");
        }
        if (usuarioDao.existeEmail(email, idIgnorado)) {
            throw new RegraNegocioException("E-mail ja cadastrado na plataforma.");
        }
    }

    // Confere o formato do e-mail percorrendo os caracteres (sem regex, só lógica própria)
    private boolean emailValido(String email) {
        if (email == null) return false;
        String e = email.trim();
        int arroba = e.indexOf('@');

        // Precisa ter exatamente um "@", com texto antes e um ponto depois dele
        if (arroba <= 0 || arroba != e.lastIndexOf('@') || e.contains(" ")) return false;
        int ponto = e.lastIndexOf('.');
        return ponto > arroba + 1 && ponto < e.length() - 1;
    }

    // ------------------------------------------------------------------
    // Missões
    // ------------------------------------------------------------------

    /* Conclui uma missão para um usuário.

       É o método que o feedback da Sprint 3 cobrava. Antes ele só marcava um boolean
       dentro do objeto da missão, que valia para todo mundo e sumia ao reiniciar.
       Agora a conclusão vira uma linha em T_SMD_USUARIO_MISSAO, e a pontuação nova
       do usuário é gravada no banco.

       A ordem importa: grava a conclusão antes de atualizar o usuário. Se a inserção
       falhar (a constraint do banco barra a conclusão repetida), o usuário não fica
       com pontos de uma missão que não foi registrada. */
    public UsuarioMissao concluirMissao(int usuarioId, int missaoId)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException, ConflitoException {
        Usuario usuario = usuarioDao.buscar(usuarioId);
        MissaoSustentabilidade missao = missaoDao.buscar(missaoId);

        if (!missao.estaValida()) {
            throw new RegraNegocioException("Missao fora do prazo: " + missao.getTitulo());
        }
        if (usuarioMissaoDao.jaConcluiu(usuarioId, missaoId)) {
            throw new ConflitoException("Este usuario ja concluiu a missao: " + missao.getTitulo());
        }

        // A própria missão aplica o efeito no usuário (soma pontos, recalcula nível,
        // zera o contador de dias) e devolve o registro da conclusão
        UsuarioMissao conclusao = missao.concluir(usuario);

        usuarioMissaoDao.registrar(conclusao);
        usuarioDao.atualizar(usuario);

        return conclusao;
    }

    // Missões que o usuário ainda não concluiu — é o catálogo que o front exibe
    public List<MissaoSustentabilidade> listarMissoesDisponiveis(int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        // Busca o usuário só para confirmar que ele existe antes de montar a lista
        usuarioDao.buscar(usuarioId);
        return missaoDao.listarDisponiveisPara(usuarioId);
    }

    // Histórico de missões concluídas pelo usuário
    public List<UsuarioMissao> listarHistoricoMissoes(int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        usuarioDao.buscar(usuarioId);
        return usuarioMissaoDao.listarPorUsuario(usuarioId);
    }

    // ------------------------------------------------------------------
    // Resgate de cupons
    // ------------------------------------------------------------------

    /* Realiza o resgate de um cupom para o usuário.

       Regras preservadas da Sprint 2: precisa estar dentro do raio de ativação da loja
       E ter concluído alguma missão nos últimos 7 dias.

       O que mudou: a contagem dos 7 dias deixou de sair do contador nr_dias_ult_missao
       gravado no usuário e passou a vir da data real da última conclusão, em
       T_SMD_USUARIO_MISSAO. O contador podia dessincronizar; a data, não. */
    public Resgate processarResgate(int usuarioId, int lojaId, double userLat, double userLon)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException, ResgateBloqueadoException {
        Usuario usuario = usuarioDao.buscar(usuarioId);
        Loja loja = lojaDao.buscar(lojaId);

        if (!loja.isAtiva()) {
            throw new RegraNegocioException("Loja desativada: " + loja.getNome());
        }

        // Primeira regra: o usuário precisa estar dentro do raio da loja
        if (!geolocalizacaoBO.verificarProximidade(userLat, userLon, loja)) {
            throw new ResgateBloqueadoException("Voce ainda nao chegou na area da oferta.");
        }

        // Segunda regra: precisa ter concluído alguma missão nos últimos 7 dias
        UsuarioMissao ultima = usuarioMissaoDao.buscarUltimaConclusao(usuarioId);
        if (ultima == null || !ultima.concluidaNosUltimos(DIAS_LIMITE_RESGATE)) {
            throw new ResgateBloqueadoException(
                    "Cupom bloqueado. Realize uma missao de sustentabilidade para liberar.");
        }

        // Todas as condições atendidas — grava o resgate e credita os pontos
        Resgate resgate = new Resgate(usuarioId, lojaId, loja.getCupomNome(),
                LocalDate.now(), PONTOS_POR_RESGATE);
        resgateDao.registrar(resgate);

        usuario.adicionarPontos(PONTOS_POR_RESGATE);
        usuarioDao.atualizar(usuario);

        return resgate;
    }

    // Histórico de cupons resgatados pelo usuário
    public List<Resgate> listarHistoricoResgates(int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        usuarioDao.buscar(usuarioId);
        return resgateDao.listarPorUsuario(usuarioId);
    }

    // A busca de lojas próximas mora na LojaBO: é consulta de catálogo, não jornada
    // do usuário. Aqui a GeolocalizacaoBO entra só para validar o raio no resgate.

    // ------------------------------------------------------------------
    // Ranking
    // ------------------------------------------------------------------

    /* Método gamificação: devolve os usuários ordenados por pontuação (maior para menor).

       Na Sprint 3 este método se chamava exibirRanking() e imprimia no console. O nome
       mudou junto com o comportamento: agora ele devolve a lista e quem exibe é o front.

       A ordenação continua sendo o Bubble Sort escrito na mão, e não Collections.sort,
       porque é um dos algoritmos próprios da solução. */
    public List<Usuario> gerarRanking() throws SQLException {
        // Copia a lista vinda do banco para não depender da ordem de cadastro
        List<Usuario> ranking = new ArrayList<>(usuarioDao.listar());

        // Ordenação por Bubble Sort — compara pares adjacentes e troca se necessário
        for (int i = 0; i < ranking.size() - 1; i++) {
            for (int j = 0; j < ranking.size() - 1 - i; j++) {
                // Se o usuário atual tem menos pontos que o próximo, troca as posições
                if (ranking.get(j).getPontuacao() < ranking.get(j + 1).getPontuacao()) {
                    Usuario temp = ranking.get(j);
                    ranking.set(j, ranking.get(j + 1));
                    ranking.set(j + 1, temp);
                }
            }
        }

        return ranking;
    }

    /* Método de negócio: quantos pontos faltam para o usuário subir de nível.
       Usa a regra que mora no próprio model. */
    public int pontosParaProximoNivel(int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        return usuarioDao.buscar(usuarioId).pontosParaProximoNivel();
    }
}
