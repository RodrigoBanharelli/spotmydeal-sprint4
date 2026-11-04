package br.com.spotmydeal.dao;

import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.factory.ConnectionFactory;
import br.com.spotmydeal.model.UsuarioMissao;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Persistência do relacionamento usuário x missão — tabela T_SMD_USUARIO_MISSAO.

   Este DAO e o ResgateDao são a resposta direta ao feedback da Sprint 3: as classes
   e tabelas existiam no MER, mas nenhuma delas tinha chegado ao código.

   Não tem método de atualizar, e é de propósito. Um registro aqui é um fato histórico:
   "fulano concluiu a missão X no dia Y e ganhou Z pontos". Editar isso depois seria
   reescrever o passado e bagunçar a pontuação do usuário. Se foi registrado errado,
   o caminho é apagar e registrar de novo. */
@Repository
public class UsuarioMissaoDao {

    // Fábrica que entrega conexões do pool configurado no application.properties
    private final ConnectionFactory connectionFactory;

    public UsuarioMissaoDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    /* CREATE — registra a conclusão de uma missão por um usuário.

       Se o par (usuário, missão) já existir, a constraint UK_SMD_UM_PAR do banco recusa
       a inserção e sobe um SQLException. A camada BO chama o jaConcluiu() antes para dar
       uma resposta melhor que um erro 500, mas a constraint continua sendo a garantia
       final — ela vale mesmo se alguém escrever direto no banco. */
    public void registrar(UsuarioMissao conclusao) throws SQLException {
        String sql = "insert into t_smd_usuario_missao (cd_usuario_missao, cd_usuario, " +
                "cd_missao, dt_conclusao, vl_pontos_ganhos) " +
                "values (sq_t_smd_usuario_missao.nextval, ?, ?, ?, ?)";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"cd_usuario_missao"})) {
            stmt.setInt(1, conclusao.getUsuarioId());
            stmt.setInt(2, conclusao.getMissaoId());
            // LocalDate do Java vira DATE do Oracle
            stmt.setDate(3, Date.valueOf(conclusao.getDataConclusao()));
            stmt.setInt(4, conclusao.getPontosGanhos());
            stmt.executeUpdate();

            try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    conclusao.setId(resultSet.getInt(1));
                }
            }
        }
    }

    // READ — busca um registro de conclusão pelo código
    public UsuarioMissao buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_usuario_missao where cd_usuario_missao = ?")) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntidadeNaoEncontradaException("Conclusao nao encontrada. Codigo: " + id);
                }
                return getUsuarioMissao(resultSet);
            }
        }
    }

    /* READ — histórico de missões concluídas por um usuário, da mais recente para a mais antiga.
       É o que responde o GET /usuarios/{id}/missoes. */
    public List<UsuarioMissao> listarPorUsuario(int usuarioId) throws SQLException {
        String sql = "select * from t_smd_usuario_missao where cd_usuario = ? " +
                "order by dt_conclusao desc, cd_usuario_missao desc";

        List<UsuarioMissao> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(getUsuarioMissao(resultSet));
                }
            }
        }
        return lista;
    }

    /* READ — responde se este usuário já concluiu esta missão.

       Usa "count(*)" em vez de trazer a linha inteira porque só interessa o sim ou não.
       É o que permite a API devolver 409 Conflict em vez de deixar estourar o erro
       de constraint do Oracle. */
    public boolean jaConcluiu(int usuarioId, int missaoId) throws SQLException {
        String sql = "select count(*) from t_smd_usuario_missao " +
                "where cd_usuario = ? and cd_missao = ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);
            stmt.setInt(2, missaoId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    /* READ — responde se alguma pessoa já concluiu esta missão.

       Serve para o MissaoBO decidir se a missão ainda pode ser excluída do catálogo:
       se já há conclusão registrada, apagar a missão levaria junto o histórico de quem
       a fez — e a chave estrangeira do banco recusaria de qualquer forma. */
    public boolean existeConclusaoParaMissao(int missaoId) throws SQLException {
        String sql = "select count(*) from t_smd_usuario_missao where cd_missao = ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, missaoId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    /* READ — última missão concluída pelo usuário, ou null se ele nunca concluiu nenhuma.

       É a base da regra dos 7 dias do resgate de cupom. Na Sprint 3 essa informação vinha
       de um contador gravado no próprio usuário (nr_dias_ult_missao), que podia
       dessincronizar; agora ela é calculada a partir do histórico real. */
    public UsuarioMissao buscarUltimaConclusao(int usuarioId) throws SQLException {
        String sql = "select * from t_smd_usuario_missao where cd_usuario = ? " +
                "order by dt_conclusao desc, cd_usuario_missao desc " +
                "fetch first 1 rows only";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                // Devolve null de propósito: "nunca concluiu nada" não é um erro,
                // é um estado normal de usuário recém-cadastrado
                if (!resultSet.next()) {
                    return null;
                }
                return getUsuarioMissao(resultSet);
            }
        }
    }

    // DELETE — desfaz o registro de uma conclusão
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "delete from t_smd_usuario_missao where cd_usuario_missao = ?")) {
            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Conclusao nao encontrada. Codigo: " + id);
            }
        }
    }

    // Monta um UsuarioMissao a partir da linha atual do ResultSet
    private static UsuarioMissao getUsuarioMissao(ResultSet resultSet) throws SQLException {
        int codigo = resultSet.getInt("cd_usuario_missao");
        int usuarioId = resultSet.getInt("cd_usuario");
        int missaoId = resultSet.getInt("cd_missao");
        int pontos = resultSet.getInt("vl_pontos_ganhos");

        // A coluna é NOT NULL no banco, mas o getDate pode voltar null se o select mudar;
        // o teste evita um NullPointerException difícil de rastrear depois
        Date data = resultSet.getDate("dt_conclusao");
        return new UsuarioMissao(codigo, usuarioId, missaoId,
                data == null ? null : data.toLocalDate(), pontos);
    }
}
