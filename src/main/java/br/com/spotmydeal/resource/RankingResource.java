package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.GamificacaoBO;
import br.com.spotmydeal.dto.DetalhesUsuarioDto;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.List;

/* Endpoint do ranking de usuários.

   Tem recurso próprio porque o ranking não é "a lista de usuários ordenada de um jeito
   diferente" — é uma visão da gamificação, com significado próprio para o front.
   Em REST, cada coisa com identidade própria ganha a sua URI. */
@RestController
@RequestMapping("/ranking")
public class RankingResource {

    private final GamificacaoBO gamificacaoBO;

    public RankingResource(GamificacaoBO gamificacaoBO) {
        this.gamificacaoBO = gamificacaoBO;
    }

    /* GET /ranking — usuários ordenados por pontuação, do maior para o menor | 200

       A ordenação é o Bubble Sort escrito na mão na GamificacaoBO, preservado da
       Sprint 3 por ser um dos algoritmos próprios da solução. */
    @GetMapping
    public List<DetalhesUsuarioDto> listar() throws SQLException {
        return gamificacaoBO.gerarRanking()
                .stream()
                .map(DetalhesUsuarioDto::de)
                .toList();
    }
}
