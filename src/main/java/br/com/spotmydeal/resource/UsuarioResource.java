package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.GamificacaoBO;
import br.com.spotmydeal.dto.DetalhesUsuarioDto;
import br.com.spotmydeal.dto.UsuarioFormDto;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.Usuario;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

/* Endpoints do recurso usuário.

   @RestController marca a classe como controlador REST: o retorno dos métodos é
   convertido para JSON automaticamente. @RequestMapping define a URL base.

   A classe não tem regra de negócio nenhuma — ela traduz HTTP em chamadas à BO e
   escolhe o status code da resposta. As validações de formato ficam nas anotações
   do DTO; as regras do domínio ficam na GamificacaoBO; as exceções viram status HTTP
   no ApiExceptionHandler. */
@RestController
@RequestMapping("/usuarios")
public class UsuarioResource {

    private final GamificacaoBO gamificacaoBO;

    // Injeção pelo construtor: com um único construtor o @Autowired é dispensável
    public UsuarioResource(GamificacaoBO gamificacaoBO) {
        this.gamificacaoBO = gamificacaoBO;
    }

    // GET /usuarios — lista todos os usuários | 200
    @GetMapping
    public List<DetalhesUsuarioDto> listar() throws SQLException {
        return gamificacaoBO.listarUsuarios()
                .stream()
                .map(DetalhesUsuarioDto::de)
                .toList();
    }

    // GET /usuarios/{id} — busca um usuário | 200 ou 404
    @GetMapping("/{id}")
    public ResponseEntity<DetalhesUsuarioDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        Usuario usuario = gamificacaoBO.buscarUsuario(id);
        return ResponseEntity.ok(DetalhesUsuarioDto.de(usuario));
    }

    /* POST /usuarios — cadastra um usuário | 201, 400

       Devolve 201 Created com o cabeçalho Location apontando para o recurso novo,
       que é o que a especificação REST pede num cadastro. O @Valid dispara as
       anotações do DTO antes do método rodar. */
    @PostMapping
    public ResponseEntity<DetalhesUsuarioDto> cadastrar(@Valid @RequestBody UsuarioFormDto dto,
                                                        UriComponentsBuilder uriBuilder)
            throws SQLException, RegraNegocioException {
        Usuario usuario = gamificacaoBO.registrarUsuario(dto.toModel());

        URI uri = uriBuilder.path("/usuarios/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();

        return ResponseEntity.created(uri).body(DetalhesUsuarioDto.de(usuario));
    }

    /* PUT /usuarios/{id} — altera nome e e-mail | 200, 400, 404

       Só nome e e-mail: pontuação e nível são do servidor. Veja o comentário do
       UsuarioFormDto para o porquê. */
    @PutMapping("/{id}")
    public ResponseEntity<DetalhesUsuarioDto> atualizar(@PathVariable int id,
                                                        @Valid @RequestBody UsuarioFormDto dto)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        Usuario usuario = gamificacaoBO.atualizarUsuario(id, dto.getNome(), dto.getEmail());
        return ResponseEntity.ok(DetalhesUsuarioDto.de(usuario));
    }

    /* DELETE /usuarios/{id} — remove o usuário | 204, 404

       204 No Content porque não há o que devolver depois de apagar. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        gamificacaoBO.removerUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
