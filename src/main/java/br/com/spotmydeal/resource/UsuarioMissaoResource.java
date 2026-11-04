package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.GamificacaoBO;
import br.com.spotmydeal.dto.ConclusaoMissaoFormDto;
import br.com.spotmydeal.dto.DetalhesConclusaoDto;
import br.com.spotmydeal.dto.DetalhesMissaoDto;
import br.com.spotmydeal.exception.ConflitoException;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.UsuarioMissao;

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

/* Endpoints das missões de um usuário — o relacionamento N:N entre usuário e missão.

   Esta classe e a UsuarioResgateResource são a resposta direta ao feedback da Sprint 3.
   Sem elas, missão e usuário continuariam sendo duas tabelas que nunca se encontram.

   A URL aninhada (/usuarios/{usuarioId}/missoes) é proposital: a conclusão não existe
   sozinha, ela é sempre de um usuário. É o que a aula chama de sub-recurso. */
@RestController
@RequestMapping("/usuarios/{usuarioId}/missoes")
public class UsuarioMissaoResource {

    private final GamificacaoBO gamificacaoBO;

    public UsuarioMissaoResource(GamificacaoBO gamificacaoBO) {
        this.gamificacaoBO = gamificacaoBO;
    }

    // GET /usuarios/{usuarioId}/missoes — histórico de missões concluídas | 200, 404
    @GetMapping
    public List<DetalhesConclusaoDto> listarHistorico(@PathVariable int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        return gamificacaoBO.listarHistoricoMissoes(usuarioId)
                .stream()
                .map(DetalhesConclusaoDto::de)
                .toList();
    }

    /* GET /usuarios/{usuarioId}/missoes/disponiveis — missões que ele ainda não fez | 200, 404

       É o catálogo que a tela de missões exibe. Fica aqui, e não no MissaoResource,
       porque o resultado depende de quem está perguntando. */
    @GetMapping("/disponiveis")
    public List<DetalhesMissaoDto> listarDisponiveis(@PathVariable int usuarioId)
            throws SQLException, EntidadeNaoEncontradaException {
        return gamificacaoBO.listarMissoesDisponiveis(usuarioId)
                .stream()
                .map(DetalhesMissaoDto::de)
                .toList();
    }

    /* POST /usuarios/{usuarioId}/missoes — conclui uma missão | 201, 400, 404, 409

       Os quatro status possíveis:
       - 201 gravou a conclusão, somou os pontos e devolve o registro criado
       - 400 a missão está fora do prazo (RegraNegocioException)
       - 404 o usuário ou a missão não existem (EntidadeNaoEncontradaException)
       - 409 este usuário já concluiu esta missão (ConflitoException)

       O 409 é o que diferencia este endpoint de um POST comum: concluir duas vezes
       não é erro de dados nem recurso inexistente, é repetição de algo já feito. */
    @PostMapping
    public ResponseEntity<DetalhesConclusaoDto> concluir(@PathVariable int usuarioId,
                                                         @Valid @RequestBody ConclusaoMissaoFormDto dto,
                                                         UriComponentsBuilder uriBuilder)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException, ConflitoException {
        UsuarioMissao conclusao = gamificacaoBO.concluirMissao(usuarioId, dto.getMissaoId());

        URI uri = uriBuilder.path("/usuarios/{usuarioId}/missoes")
                .buildAndExpand(usuarioId)
                .toUri();

        return ResponseEntity.created(uri).body(DetalhesConclusaoDto.de(conclusao));
    }
}
