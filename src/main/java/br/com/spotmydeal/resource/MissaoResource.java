package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.MissaoBO;
import br.com.spotmydeal.dto.DetalhesMissaoDto;
import br.com.spotmydeal.dto.MissaoFormDto;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.MissaoSustentabilidade;

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

/* Endpoints do recurso missão — o catálogo.

   As missões de um usuário específico (histórico e disponíveis) ficam no
   UsuarioMissaoResource, porque dependem de quem está perguntando. */
@RestController
@RequestMapping("/missoes")
public class MissaoResource {

    private final MissaoBO missaoBO;

    public MissaoResource(MissaoBO missaoBO) {
        this.missaoBO = missaoBO;
    }

    // GET /missoes — lista o catálogo | 200
    @GetMapping
    public List<DetalhesMissaoDto> listar() throws SQLException {
        return missaoBO.listar()
                .stream()
                .map(DetalhesMissaoDto::de)
                .toList();
    }

    // GET /missoes/{id} — busca uma missão | 200, 404
    @GetMapping("/{id}")
    public ResponseEntity<DetalhesMissaoDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(DetalhesMissaoDto.de(missaoBO.buscar(id)));
    }

    // POST /missoes — cadastra uma missão | 201, 400
    @PostMapping
    public ResponseEntity<DetalhesMissaoDto> cadastrar(@Valid @RequestBody MissaoFormDto dto,
                                                       UriComponentsBuilder uriBuilder)
            throws SQLException {
        MissaoSustentabilidade missao = missaoBO.cadastrar(dto.toModel());

        URI uri = uriBuilder.path("/missoes/{id}")
                .buildAndExpand(missao.getId())
                .toUri();

        return ResponseEntity.created(uri).body(DetalhesMissaoDto.de(missao));
    }

    // PUT /missoes/{id} — altera a missão | 200, 400, 404
    @PutMapping("/{id}")
    public ResponseEntity<DetalhesMissaoDto> atualizar(@PathVariable int id,
                                                       @Valid @RequestBody MissaoFormDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(DetalhesMissaoDto.de(missaoBO.atualizar(id, dto.toModel())));
    }

    /* DELETE /missoes/{id} — remove a missão | 204, 400, 404

       Devolve 400 quando alguém já concluiu a missão, para não apagar o histórico.
       Nesse caso a saída é zerar o prazo em dias pelo PUT. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        missaoBO.apagar(id);
        return ResponseEntity.noContent().build();
    }
}
