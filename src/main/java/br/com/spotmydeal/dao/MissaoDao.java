package br.com.spotmydeal.dao;

import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.factory.ConnectionFactory;
import br.com.spotmydeal.model.MissaoSustentabilidade;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Persistência das missões de sustentabilidade — tabela T_SMD_MISSAO.

   Repare que não há coluna nem campo de "concluída": a missão é só o catálogo.
   Quem registra que um usuário concluiu é o UsuarioMissaoDao, na tabela de
   relacionamento. Era exatamente isso que faltava na Sprint 3. */
@Repository
public class MissaoDao {

    // Fábrica que entrega conexões do pool configurado no application.properties
    private final ConnectionFactory connectionFactory;

    public MissaoDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // CRUD (Create, Read, Update, Delete)

    // CREATE insere a missão e devolve o código gerado pela sequence no próprio objeto
    public void cadastrar(MissaoSustentabilidade missao) throws SQLException {
        String sql = "insert into t_smd_missao (cd_missao, nm_titulo, ds_missao, " +
                "vl_pontos, nr_dias_limite) " +
                "values (sq_t_smd_missao.nextval, ?, ?, ?, ?)";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"cd_missao"})) {
            stmt.setString(1, missao.getTitulo());
            stmt.setString(2, missao.getDescricao());
            stmt.setInt(3, missao.getPontosRecompensa());
            stmt.setInt(4, missao.getDiasLimite());
            stmt.executeUpdate();

            // Recupera o código gerado pela sequence e atualiza o objeto em memória
            try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    missao.setId(resultSet.getInt(1));
                }
            }
        }
    }

    // READ — busca uma única missão pelo código; lança exceção se não existir
    public MissaoSustentabilidade buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_missao where cd_missao = ?")) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                if (!resultSet.next()) {
                    throw new EntidadeNaoEncontradaException("Missao nao encontrada. Codigo: " + id);
                }
                return getMissao(resultSet);
            }
        }
    }

    // READ — lista todo o catálogo de missões
    public List<MissaoSustentabilidade> listar() throws SQLException {
        List<MissaoSustentabilidade> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_missao order by cd_missao");
             ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                lista.add(getMissao(resultSet));
            }
        }
        return lista;
    }

    /* READ — lista as missões que um usuário ainda NÃO concluiu.

       É o que o front precisa mostrar na tela de missões disponíveis: sem esse filtro,
       o usuário veria missões que já fez. O "not exists" resolve isso no banco,
       olhando a tabela de relacionamento. */
    public List<MissaoSustentabilidade> listarDisponiveisPara(int usuarioId) throws SQLException {
        String sql = "select m.* from t_smd_missao m " +
                "where not exists (select 1 from t_smd_usuario_missao um " +
                "                  where um.cd_missao = m.cd_missao and um.cd_usuario = ?) " +
                "order by m.cd_missao";

        List<MissaoSustentabilidade> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setInt(1, usuarioId);

            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    lista.add(getMissao(resultSet));
                }
            }
        }
        return lista;
    }

    // UPDATE — grava no banco o estado atual do objeto
    public void atualizar(MissaoSustentabilidade missao) throws SQLException, EntidadeNaoEncontradaException {
        String sql = "update t_smd_missao set nm_titulo = ?, ds_missao = ?, " +
                "vl_pontos = ?, nr_dias_limite = ? where cd_missao = ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, missao.getTitulo());
            stmt.setString(2, missao.getDescricao());
            stmt.setInt(3, missao.getPontosRecompensa());
            stmt.setInt(4, missao.getDiasLimite());
            stmt.setInt(5, missao.getId());

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Missao nao encontrada. Codigo: " + missao.getId());
            }
        }
    }

    /* DELETE — remove a missão pelo código.

       Igual à loja: se a missão já tiver sido concluída por alguém, a chave estrangeira
       de T_SMD_USUARIO_MISSAO impede a exclusão e o Oracle sobe um SQLException.
       O histórico do usuário vale mais do que a limpeza do catálogo. */
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "delete from t_smd_missao where cd_missao = ?")) {
            stmt.setInt(1, id);

            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Missao nao encontrada. Codigo: " + id);
            }
        }
    }

    // Monta uma MissaoSustentabilidade a partir da linha atual do ResultSet
    private static MissaoSustentabilidade getMissao(ResultSet resultSet) throws SQLException {
        int codigo = resultSet.getInt("cd_missao");
        String titulo = resultSet.getString("nm_titulo");
        String descricao = resultSet.getString("ds_missao");
        int pontos = resultSet.getInt("vl_pontos");
        int diasLimite = resultSet.getInt("nr_dias_limite");
        return new MissaoSustentabilidade(codigo, titulo, descricao, pontos, diasLimite);
    }
}
