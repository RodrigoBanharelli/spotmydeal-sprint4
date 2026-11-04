package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.GamificacaoBO;
import br.com.spotmydeal.dto.DetalhesResgateDto;
import br.com.spotmydeal.dto.ResgateFormDto;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.exception.ResgateBloqueadoException;
import br.com.spotmydeal.model.Resgate;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

/* Endpoints dos resgates de cupom de um usuário — o relacionamento N:N entre
   usuário e loja.

   É aqui que as duas regras herdadas da Sprint 2 finalmente deixam rastro no banco:
   antes elas só decidiam o que imprimir no console. */
@RestController
@RequestMapping("/usuarios/{usuarioId}/resgates")
public class UsuarioResgateResource {

    private final GamificacaoBO gamificacaoBO;

    public UsuarioResgateResource(GamificacaoBO gamificacaoBO) {
        this.gamificacaoBO = gamificacaoBO;
    }

    // GET /usuarios/{usuarioId}/resgates — histórico de cupons resgatados | 200, 404
    @GetMapping
    public List<DetalhesResgateDto> listarHistorico(@PathVariable int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        return gamificacaoBO.listarHistoricoResgates(usuarioId)
                .stream()
                .map(DetalhesResgateDto::de)
                .toList();
    }

    /* POST /usuarios/{usuarioId}/resgates — resgata o cupom de uma loja | 201, 400, 403, 404

       Os quatro status possíveis:
       - 201 gravou o resgate e creditou os pontos
       - 400 a loja está desativada (RegraNegocioException)
       - 403 o pedido está correto, mas o usuário não tem direito agora
             (ResgateBloqueadoException): ou está fora do raio da loja, ou não conclui
             missão há mais de 7 dias
       - 404 o usuário ou a loja não existem

       O 403 em vez de 400 é a decisão de desenho aqui: os dados enviados estão certos,
       o que falta é permissão. */
    @PostMapping
    public ResponseEntity<DetalhesResgateDto> resgatar(@PathVariable int usuarioId,
                                                       @Valid @RequestBody ResgateFormDto dto,
                                                       UriComponentsBuilder uriBuilder)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException,
            ResgateBloqueadoException {
        Resgate resgate = gamificacaoBO.processarResgate(usuarioId, dto.getLojaId(),
                dto.getLatitude(), dto.getLongitude());

        URI uri = uriBuilder.path("/usuarios/{usuarioId}/resgates")
                .buildAndExpand(usuarioId)
                .toUri();

        return ResponseEntity.created(uri).body(DetalhesResgateDto.de(resgate));
    }
}
