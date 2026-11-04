package br.com.spotmydeal.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/* Liberação de CORS para o front.

   O navegador bloqueia por padrão qualquer chamada fetch/AJAX para um domínio
   diferente do da página. Como o front do SoulUp roda noutro endereço (local no
   desenvolvimento, e outro host depois do deploy), sem esta classe toda requisição
   do front seria barrada antes mesmo de chegar na API — e o erro apareceria só no
   console do navegador, não no log do servidor.

   O allowedOrigins("*") é o que a apostila 18 usa e é adequado para o escopo do
   challenge, já que a API não guarda sessão nem cookie. Num sistema com login, o
   correto seria listar os domínios do front explicitamente.

   Apostila 18, p.8. */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
