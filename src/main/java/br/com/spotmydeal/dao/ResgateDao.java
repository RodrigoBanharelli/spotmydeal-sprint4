package br.com.spotmydeal.dao;

import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.factory.ConnectionFactory;
import br.com.spotmydeal.model.Resgate;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Persistência do relacionamento usuário x loja — tabela T_SMD_RESGATE.

   Na Sprint 3 o processarResgate() só imprimia "OFERTA ATIVADA" no console e somava
   pontos; nada ficava gravado. Sem isso não havia histórico de cupons, e o front não
   tinha como mostrar o que o usuário já resgatou.

   Diferente do UsuarioMissaoDao, aqui não existe restrição de unicidade: o mesmo
   usuário pode resgatar na mesma loja várias vezes, em datas diferentes.

   Também não tem atualizar, pelo mesmo motivo do outro: resgate é fato histórico. */
@Repository
public class ResgateDao {

    // Fábrica que entrega conexões do pool configurado no application.properties
    private final ConnectionFactory connectionFactory;

    public ResgateDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /* CREATE — registra o resgate de um cupom.

       O texto do cupom é gravado aqui junto, copiado da loja no momento do resgate.
       Poderia ser lido da loja na hora de exibir o histórico, mas aí, se a loja trocasse
       a oferta, o histórico passaria a mostrar um cupom que o usuário nunca resgatou. */
    public void registrar(Resgate resgate) throws SQLException {
        String sql = "insert into t_smd_resgate (cd_resgate, cd_usuario, cd_loja, " +
                "ds_cupom, dt_resgate, vl_pontos_ganhos) " +
                "values (sq_t_smd_resgate.nextval, ?, ?, ?, ?, ?)";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"cd_resgate"})) {
            stmt.setInt(1, resgate.getUsuarioId());
            stmt.setInt(2, resgate.getLojaId());
            stmt.setString(3, resgate.getCupom());
            // LocalDate do Java vira DATE do Oracle
            stmt.setDate(4, Date.valueOf(resgate.getDataResgate()));
            stmt.setInt(5, resgate.getPontosGanhos());
            stmt.executeUpdate();

            try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    resgate.setId(resultSet.getInt(1));
                }
            }
        }
    }

    // READ — busca um resgate pelo código
    public Resgate buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_resgate where cd_resgate = ?")) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntidadeNaoEncontradaException("Resgate nao encontrado. Codigo: " + id);
                }
                return getResgate(resultSet);
            }
        }
    }

    /* READ — histórico de cupons resgatados por um usuário, do mais recente para o mais antigo.
       É o que responde o GET /usuarios/{id}/resgates. */
    public List<Resgate> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "select * from t_smd_resgate where cd_usuario = ? " +
                "order by dt_resgate desc, cd_resgate desc";

        List<Resgate> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(getResgate(resultSet));
                }
            }
        }
        return lista;
    }

    /* READ — resgates feitos numa loja, do mais recente para o mais antigo.
       Serve para a loja parceira acompanhar quantos cupons foram usados. */
    public List<Resgate> listarPorLoja(int lojaId) throws SQLException {
        String sql = "select * from t_smd_resgate where cd_loja = ? " +
                "order by dt_resgate desc, cd_resgate desc";

        List<Resgate> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, lojaId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(getResgate(resultSet));
                }
            }
        }
        return lista;
    }

    // DELETE — desfaz o registro de um resgate
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "delete from t_smd_resgate where cd_resgate = ?")) {
            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Resgate nao encontrado. Codigo: " + id);
            }
        }
    }

    // Monta um Resgate a partir da linha atual do ResultSet
    private static Resgate getResgate(ResultSet resultSet) throws SQLException {
        int codigo = resultSet.getInt("cd_resgate");
        int usuarioId = resultSet.getInt("cd_usuario");
        int lojaId = resultSet.getInt("cd_loja");
        String cupom = resultSet.getString("ds_cupom");
        int pontos = resultSet.getInt("vl_pontos_ganhos");

        Date data = resultSet.getDate("dt_resgate");
        return new Resgate(codigo, usuarioId, lojaId, cupom,
                data == null ? null : data.toLocalDate(), pontos);
    }
}
