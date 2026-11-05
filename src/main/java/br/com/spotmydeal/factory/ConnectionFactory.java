package br.com.spotmydeal.factory;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// Fábrica de conexões com o banco.
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
