package br.com.spotmydeal.bo;

import br.com.spotmydeal.dao.MissaoDao;
import br.com.spotmydeal.dao.UsuarioMissaoDao;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.MissaoSustentabilidade;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/* Regras do catálogo de missões de sustentabilidade.

   Cuida só do cadastro das missões. Concluir uma missão é jornada do usuário e mora
   na GamificacaoBO. */
@Service
public class MissaoBO {

    private final MissaoDao missaoDao;
    private final UsuarioMissaoDao usuarioMissaoDao;

    public MissaoBO(MissaoDao missaoDao, UsuarioMissaoDao usuarioMissaoDao) {
        this.missaoDao = missaoDao;
        this.usuarioMissaoDao = usuarioMissaoDao;
    }

    public List<MissaoSustentabilidade> listar() throws SQLException {
        return missaoDao.listar();
    }

    public MissaoSustentabilidade buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        return missaoDao.buscar(id);
    }

    public MissaoSustentabilidade cadastrar(MissaoSustentabilidade missao) throws SQLException {
        missaoDao.cadastrar(missao);
        return missao;
    }

    public MissaoSustentabilidade atualizar(int id, MissaoSustentabilidade dados)
            throws SQLException, EntidadeNaoEncontradaException {
        MissaoSustentabilidade missao = missaoDao.buscar(id);

        missao.setTitulo(dados.getTitulo());
        missao.setDescricao(dados.getDescricao());
        missao.setPontosRecompensa(dados.getPontosRecompensa());
        missao.setDiasLimite(dados.getDiasLimite());

        missaoDao.atualizar(missao);
        return missao;
    }

    /* Remove a missão, desde que ninguém a tenha concluído.

       Mesma lógica da loja: apagar uma missão já concluída apagaria o histórico de
       quem fez, e os pontos ficariam sem origem. Para tirar uma missão de circulação
       sem perder o histórico, o caminho é zerar o prazo com o atualizar() — o
       estaValida() do model passa a devolver false e ela some das disponíveis. */
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        MissaoSustentabilidade missao = missaoDao.buscar(id);

        if (usuarioMissaoDao.existeConclusaoParaMissao(id)) {
            throw new RegraNegocioException("A missao " + missao.getTitulo()
                    + " ja foi concluida por algum usuario e nao pode ser excluida. "
                    + "Zere o prazo em dias para tira-la de circulacao.");
        }

        missaoDao.apagar(id);
    }
}
