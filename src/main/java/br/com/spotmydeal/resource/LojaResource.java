package br.com.spotmydeal.resource;

import br.com.spotmydeal.bo.LojaBO;
import br.com.spotmydeal.dto.DetalhesLojaDto;
import br.com.spotmydeal.dto.LojaFormDto;
import br.com.spotmydeal.exception.EntidadeNaoEncontradaException;
import br.com.spotmydeal.exception.RegraNegocioException;
import br.com.spotmydeal.model.Loja;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.sql.SQLException;
import java.util.List;

// Endpoints do recurso loja
@RestController
@RequestMapping("/lojas")
public class LojaResource {

    private final LojaBO lojaBO;

    public LojaResource(LojaBO lojaBO) {
        this.lojaBO = lojaBO;
    }

    /* GET /lojas — lista as lojas | 200

       O parâmetro apenasAtivas é opcional e vem na query string: /lojas?apenasAtivas=true.
       Sem ele, devolve tudo — a tela de administração precisa ver as desativadas também. */
    @GetMapping
    public List<DetalhesLojaDto> listar(
            @RequestParam(defaultValue = "false") boolean apenasAtivas) throws SQLException {
        List<Loja> lojas = apenasAtivas ? lojaBO.listarAtivas() : lojaBO.listar();
        return lojas.stream().map(DetalhesLojaDto::de).toList();
    }

    /* GET /lojas/proximas?lat=&lon=&raio= — lojas ativas dentro do raio | 200

       Ordenadas da mais perto para a mais longe, pelo Haversine da GeolocalizacaoBO.
       O raio tem padrão de 200 metros, que é o raio de ativação histórico do SpotMyDeal.

       Este mapeamento vem antes do /{id} no arquivo por organização; o Spring resolve
       a ambiguidade sozinho porque "proximas" é um trecho literal e ganha do coringa. */
    @GetMapping("/proximas")
    public List<DetalhesLojaDto> listarProximas(@RequestParam double lat,
                                                @RequestParam double lon,
                                                @RequestParam(defaultValue = "200") double raio)
            throws SQLException {
        return lojaBO.listarProximas(lat, lon, raio)
                .stream()
                .map(DetalhesLojaDto::de)
                .toList();
    }

    // GET /lojas/{id} — busca uma loja | 200, 404
    @GetMapping("/{id}")
    public ResponseEntity<DetalhesLojaDto> buscar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(DetalhesLojaDto.de(lojaBO.buscar(id)));
    }

    // POST /lojas — cadastra uma loja | 201, 400
    @PostMapping
    public ResponseEntity<DetalhesLojaDto> cadastrar(@Valid @RequestBody LojaFormDto dto,
                                                     UriComponentsBuilder uriBuilder)
            throws SQLException {
        Loja loja = lojaBO.cadastrar(dto.toModel());

        URI uri = uriBuilder.path("/lojas/{id}")
                .buildAndExpand(loja.getId())
                .toUri();

        return ResponseEntity.created(uri).body(DetalhesLojaDto.de(loja));
    }

    // PUT /lojas/{id} — altera a loja | 200, 400, 404
    @PutMapping("/{id}")
    public ResponseEntity<DetalhesLojaDto> atualizar(@PathVariable int id,
                                                     @Valid @RequestBody LojaFormDto dto)
            throws SQLException, EntidadeNaoEncontradaException {
        return ResponseEntity.ok(DetalhesLojaDto.de(lojaBO.atualizar(id, dto.toModel())));
    }

    /* DELETE /lojas/{id} — remove a loja | 204, 400, 404

       Devolve 400 quando a loja já tem resgates: nesse caso o certo é desativar,
       para não apagar o histórico de quem resgatou. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> apagar(@PathVariable int id)
            throws SQLException, EntidadeNaoEncontradaException, RegraNegocioException {
        lojaBO.apagar(id);
        return ResponseEntity.noContent().build();
    }
}
