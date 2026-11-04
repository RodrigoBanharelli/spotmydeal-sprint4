package br.com.spotmydeal.dao;

import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.factory.ConnectionFactory;
import br.com.spotmydeal.model.Usuario;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/* Não contém regra de negócio: só traduz objetos Java em linhas da tabela e vice-versa.

   O que mudou da Sprint 3 para a Sprint 4:
   - a classe virou um @Repository, então o Spring cria e injeta a instância sozinho;
   - a conexão deixou de ser um atributo aberto no construtor e passou a ser aberta
     dentro de cada método, no try-with-resources. Numa API isso é obrigatório: várias
     requisições rodam ao mesmo tempo e não podem disputar a mesma Connection;
   - por isso o antigo fecharConexao() saiu — quem devolve a conexão ao pool é o
     próprio try-with-resources no fim de cada método. */
@Repository
public class UsuarioDao {

    // Fábrica que entrega conexões do pool configurado no application.properties
    private final ConnectionFactory connectionFactory;

    // Injeção pelo construtor: com um único construtor o @Autowired é dispensável
    public UsuarioDao(ConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    // CRUD (Create, Read, Update, Delete)

    // CREATE insere o usuário e devolve o código gerado pela sequence no próprio objeto
    public void cadastrar(Usuario usuario) throws SQLException {
        String sql = "insert into t_smd_usuario (cd_usuario, nm_usuario, ds_email, " +
                "vl_pontuacao, ds_nivel, nr_dias_ult_missao) " +
                "values (sq_t_smd_usuario.nextval, ?, ?, ?, ?, ?)";

        // PreparedStatement com "?" protege contra SQL Injection (os valores nunca são concatenados)
        // try-with-resources fecha o statement e devolve a conexão ao pool, mesmo se der erro
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql, new String[]{"cd_usuario"})) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setInt(3, usuario.getPontuacao());
            stmt.setString(4, usuario.getNivel());
            stmt.setInt(5, usuario.getDiasDesdeUltimaMissao());
            stmt.executeUpdate();

            // Recupera o código gerado pela sequence e atualiza o objeto em memória
            try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                if (resultSet.next()) {
                    usuario.setId(resultSet.getInt(1));
                }
            }
        }
    }

    // READ — busca um único usuário pelo código; lança exceção se não existir
    public Usuario buscar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_usuario where cd_usuario = ?")) {
            stmt.setInt(1, id);

            try (ResultSet resultSet = stmt.executeQuery()) {
                // Se não veio nenhuma linha, o código informado não existe na tabela
                if (!resultSet.next()) {
                    throw new EntidadeNaoEncontradaException("Usuario nao encontrado. Codigo: " + id);
                }
                return getUsuario(resultSet);
            }
        }
    }

    // READ — lista todos os usuários na ordem de cadastro (a ordenação por pontos é regra da BO)
    public List<Usuario> listar() throws SQLException {
        List<Usuario> lista = new ArrayList<>();

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "select * from t_smd_usuario order by cd_usuario");
             ResultSet resultSet = stmt.executeQuery()) {
            // Percorre todos os registros e reconstrói cada Usuario
            while (resultSet.next()) {
                lista.add(getUsuario(resultSet));
            }
        }
        return lista;
    }

    // UPDATE — grava no banco o estado atual do objeto (dados, pontuação, nível e dias)
    public void atualizar(Usuario usuario) throws SQLException, EntidadeNaoEncontradaException {
        String sql = "update t_smd_usuario set nm_usuario = ?, ds_email = ?, vl_pontuacao = ?, " +
                "ds_nivel = ?, nr_dias_ult_missao = ? where cd_usuario = ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, usuario.getNome());
            stmt.setString(2, usuario.getEmail());
            stmt.setInt(3, usuario.getPontuacao());
            stmt.setString(4, usuario.getNivel());
            stmt.setInt(5, usuario.getDiasDesdeUltimaMissao());
            stmt.setInt(6, usuario.getId());

            // executeUpdate devolve quantas linhas mudaram — zero significa código inexistente
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Usuario nao encontrado. Codigo: " + usuario.getId());
            }
        }
    }

    // DELETE — remove o usuário pelo código
    public void apagar(int id) throws SQLException, EntidadeNaoEncontradaException {
        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(
                     "delete from t_smd_usuario where cd_usuario = ?")) {
            stmt.setInt(1, id);

            // Valida se realmente apagou algo no banco
            int linhas = stmt.executeUpdate();
            if (linhas == 0) {
                throw new EntidadeNaoEncontradaException("Usuario nao encontrado. Codigo: " + id);
            }
        }
    }

    /* READ — responde se o e-mail já pertence a algum usuário.

       O parâmetro idIgnorado existe por causa da alteração de cadastro: quando o usuário
       salva o próprio perfil sem trocar o e-mail, o e-mail dele aparece no banco e não
       pode ser tratado como duplicado. Passando o id dele aqui, a própria linha é
       descartada da conta. Para um cadastro novo, basta passar -1.

       Na Sprint 3 essa checagem percorria a lista em memória do service; agora pergunta
       direto ao banco, que é a única fonte confiável quando há mais de um cliente. */
    public boolean existeEmail(String email, int idIgnorado) throws SQLException {
        String sql = "select count(*) from t_smd_usuario " +
                "where lower(ds_email) = lower(?) and cd_usuario <> ?";

        try (Connection conexao = connectionFactory.getConnection();
             PreparedStatement stmt = conexao.prepareStatement(sql)) {
            stmt.setString(1, email == null ? "" : email.trim());
            stmt.setInt(2, idIgnorado);

            try (ResultSet resultSet = stmt.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    // Monta um Usuario a partir da linha atual do ResultSet (evita repetir o mesmo código em cada busca)
    private static Usuario getUsuario(ResultSet resultSet) throws SQLException {
        int codigo = resultSet.getInt("cd_usuario");
        String nome = resultSet.getString("nm_usuario");
        String email = resultSet.getString("ds_email");
        int pontuacao = resultSet.getInt("vl_pontuacao");
        String nivel = resultSet.getString("ds_nivel");
        int dias = resultSet.getInt("nr_dias_ult_missao");
        return new Usuario(codigo, nome, email, pontuacao, nivel, dias);
    }
}
