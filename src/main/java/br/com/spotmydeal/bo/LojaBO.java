package br.com.spotmydeal.bo;

import br.com.spotmydeal.dao.LojaDao;
import br.com.spotmydeal.dao.ResgateDao;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.Loja;

import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

/* Regras do catálogo de lojas parceiras.

   Ficou separada da GamificacaoBO de propósito: gerenciar o catálogo é trabalho de
   quem administra as lojas, enquanto a GamificacaoBO cuida da jornada do usuário.
   Juntar as duas numa classe só faria uma BO com nove dependências. */
@Service
public class LojaBO {

    private final LojaDao lojaDao;
    private final ResgateDao resgateDao;
    private final GeolocalizacaoBO geolocalizacaoBO;

    public LojaBO(LojaDao lojaDao, ResgateDao resgateDao, GeolocalizacaoBO geolocalizacaoBO) {
        this.lojaDao = lojaDao;
        this.resgateDao = resgateDao;
        this.geolocalizacaoBO = geolocalizacaoBO;
    }

    public List<Loja> listar() throws SQLException {
        return lojaDao.listar();
    }

    public List<Loja> listarAtivas() throws SQLException {
        return lojaDao.listarAtivas();
    }

    public Loja buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        return lojaDao.buscar(id);
    }

    public Loja cadastrar(Loja loja) throws SQLException {
        lojaDao.cadastrar(loja);
        return loja;
    }

    /* Altera os dados da loja.

       Busca antes de atualizar para que um id inexistente vire 404 já na leitura,
       com a mesma mensagem dos outros endpoints. */
    public Loja atualizar(int id, Loja dados)
            throws SQLException, EntidadeNaoEncontradaException {
        Loja loja = lojaDao.buscar(id);

        loja.setNome(dados.getNome());
        loja.setLatitude(dados.getLatitude());
        loja.setLongitude(dados.getLongitude());
        loja.setCupomNome(dados.getCupomNome());
        loja.setAtiva(dados.isAtiva());

        lojaDao.atualizar(loja);
        return loja;
    }

    /* Remove a loja, desde que ela nunca tenha tido cupom resgatado.

       Sem esta checagem, a chave estrangeira de T_SMD_RESGATE recusaria o delete e o
       usuário do front receberia um 500 com texto de erro do Oracle. Aqui ele recebe
       400 com a explicação e a saída correta: desativar a loja preserva o histórico
       de quem já resgatou e tira a loja do ar do mesmo jeito. */
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Loja loja = lojaDao.buscar(id);

        if (!resgateDao.listarPorLoja(id).isEmpty()) {
            throw new RegraNegocioException("A loja " + loja.getNome()
                    + " possui resgates registrados e nao pode ser excluida. Desative-a no lugar.");
        }

        lojaDao.apagar(id);
    }

    // Lojas ativas dentro de um raio, da mais perto para a mais longe
    public List<Loja> listarProximas(double userLat, double userLon, double raio) throws SQLException {
        return geolocalizacaoBO.filtrarPorRaio(userLat, userLon, raio, lojaDao.listarAtivas());
    }
}
