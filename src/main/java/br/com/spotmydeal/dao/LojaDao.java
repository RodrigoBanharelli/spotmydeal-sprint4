package br.com.spotmydeal.dao;

import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.factory.ConnectionFactory;
import br.com.spotmydeal.model.Loja;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Persistência das lojas parceiras — tabela T_SMD_LOJA.

   Segue o mesmo padrão do UsuarioDao: nada de regra de negócio aqui, só a tradução
   entre o objeto Loja e a linha da tabela.

   Detalhe do Oracle: não existe tipo booleano, então a coluna st_ativa é NUMBER(1)
   com 1 para ativa e 0 para inativa. A conversão para o boolean do model acontece
   nos dois sentidos dentro desta classe, para o resto do sistema não precisar saber. */
@Repository
public class LojaDao {

    // Fábrica que entrega conexões do pool configurado no application.properties
    private final ConnectionFactory connectionFactory;

    public LojaDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // CRUD (Create, Read, Update, Delete)

    // CREATE insere a loja e devolve o código gerado pela sequence no próprio objeto
    public void cadastrar(Loja loja) throws SQLException {
        String sql = "insert into t_smd_loja (cd_loja, nm_loja, vl_latitude, vl_longitude, " +
                "ds_cupom, st_ativa) " +
                "values (sq_t_smd_loja.nextval, ?, ?, ?, ?, ?)";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"cd_loja"})) {
            stmt.setString(1, loja.getNome());
            stmt.setDouble(2, loja.getLatitude());
            stmt.setDouble(3, loja.getLongitude());
            stmt.setString(4, loja.getCupomNome());
            stmt.setInt(5, loja.isAtiva() ? 1 : 0);
            stmt.executeUpdate();

            // Recupera o código gerado pela sequence e atualiza o objeto em memória
            try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    loja.setId(resultSet.getInt(1));
                }
            }
        }
    }

    // READ — busca uma única loja pelo código; lança exceção se não existir
    public Loja buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_loja where cd_loja = ?")) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntidadeNaoEncontradaException("Loja nao encontrada. Codigo: " + id);
                }
                return getLoja(resultSet);
            }
        }
    }

    // READ — lista todas as lojas, ativas e inativas (a tela de administração precisa das duas)
    public List<Loja> listar() throws SQLException {
        return listarPorFiltro("select * from t_smd_loja order by cd_loja");
    }

    /* READ — lista só as lojas ativas.
       É esta a lista que a camada BO percorre para calcular a loja mais próxima:
       filtrar no banco evita trazer loja desativada só para descartar depois em Java. */
    public List<Loja> listarAtivas() throws SQLException {
        return listarPorFiltro("select * from t_smd_loja where st_ativa = 1 order by cd_loja");
    }

    // UPDATE — grava no banco o estado atual do objeto
    public void atualizar(Loja loja) throws SQLException, EntidadeNaoEncontradaException {
        String sql = "update t_smd_loja set nm_loja = ?, vl_latitude = ?, vl_longitude = ?, " +
                "ds_cupom = ?, st_ativa = ? where cd_loja = ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, loja.getNome());
            stmt.setDouble(2, loja.getLatitude());
            stmt.setDouble(3, loja.getLongitude());
            stmt.setString(4, loja.getCupomNome());
            stmt.setInt(5, loja.isAtiva() ? 1 : 0);
            stmt.setInt(6, loja.getId());

            // executeUpdate devolve quantas linhas mudaram — zero significa código inexistente
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Loja nao encontrada. Codigo: " + loja.getId());
            }
        }
    }

    /* DELETE — remove a loja pelo código.

       Atenção: se a loja já tiver resgates em T_SMD_RESGATE, o Oracle recusa a exclusão
       por causa da chave estrangeira e sobe um SQLException. Isso é proposital — apagar
       a loja apagaria o histórico do usuário junto. Nesse caso o certo é desativar a loja
       (st_ativa = 0) com o atualizar(), não excluir. */
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "delete from t_smd_loja where cd_loja = ?")) {
            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Loja nao encontrada. Codigo: " + id);
            }
        }
    }

    // Executa uma consulta que devolve várias lojas (evita repetir o mesmo laço nos dois listar)
    private List<Loja> listarPorFiltro(String sql) throws SQLException {
        List<Loja> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql);
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                lista.add(getLoja(resultSet));
            }
        }
        return lista;
    }

    // Monta uma Loja a partir da linha atual do ResultSet
    private static Loja getLoja(ResultSet resultSet) throws SQLException {
        int codigo = resultSet.getInt("cd_loja");
        String nome = resultSet.getString("nm_loja");
        double latitude = resultSet.getDouble("vl_latitude");
        double longitude = resultSet.getDouble("vl_longitude");
        String cupom = resultSet.getString("ds_cupom");

        // Converte o NUMBER(1) do Oracle para o boolean do model
        boolean ativa = resultSet.getInt("st_ativa") == 1;

        return new Loja(codigo, nome, latitude, longitude, cupom, ativa);
    }
}
