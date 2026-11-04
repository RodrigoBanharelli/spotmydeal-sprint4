package br.com.spotmydeal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Classe de entrada da API. A anotacao @SpringBootApplication liga a configuracao
// automatica, sobe o Tomcat embarcado e varre os pacotes abaixo de br.com.spotmydeal
// procurando os @Repository, @Service e @RestController.
@SpringBootApplication
public class SoulupApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoulupApiApplication.class, args);
	}

}
