package br.com.spotmydeal.factory;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/* Fábrica de conexões com o banco.

   Mudou de forma da Sprint 3 para a Sprint 4. Antes, esta classe abria a conexão
   na mão com DriverManager e carregava a URL, o usuário e a senha dentro do código.
   Agora quem monta o pool de conexões é o Spring, a partir do application.properties,
   e esta classe só entrega uma conexão desse pool.

   Ganhos: as credenciais saem do código-fonte (importante porque o repositório da
   Sprint 4 é público) e o pool reaproveita conexões em vez de abrir uma por requisição. */
@Component
public class ConnectionFactory {

    // Pool de conexões criado pelo Spring com os dados do application.properties
    private final DataSource dataSource;

    // Injeção pelo construtor: com um único construtor o @Autowired é dispensável
    public ConnectionFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    // Entrega uma conexão do pool. Quem chama fecha com try-with-resources,
    // e fechar devolve a conexão para o pool em vez de encerrar de verdade.
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
