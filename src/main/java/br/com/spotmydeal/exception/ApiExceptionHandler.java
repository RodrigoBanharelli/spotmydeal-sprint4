package br.com.spotmydeal.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.sql.SQLException;

/* Tradutor central de exceção para status HTTP.

   @RestControllerAdvice vale para todos os Resources de uma vez: nenhum deles precisa
   de try/catch. Cada Resource declara o que pode lançar e a decisão de qual status
   devolver mora aqui, num lugar só.

   Apostila 17, p.26 a 29. */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    // 404 — o recurso pedido não existe no banco
    @ExceptionHandler(EntidadeNaoEncontradaException.class)
    public ResponseEntity<ErrorResponseDto> naoEncontrada(EntidadeNaoEncontradaException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponseDto("Entidade nao encontrada", e.getMessage()));
    }

    // 400 — os dados chegaram completos, mas ferem uma regra do SoulUp
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErrorResponseDto> regraDeNegocio(RegraNegocioException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Regra de negocio", e.getMessage()));
    }

    // 403 — o pedido está correto, o que falta é direito de fazer agora
    @ExceptionHandler(ResgateBloqueadoException.class)
    public ResponseEntity<ErrorResponseDto> resgateBloqueado(ResgateBloqueadoException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponseDto("Resgate bloqueado", e.getMessage()));
    }

    // 409 — a operação já havia sido feita antes
    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErrorResponseDto> conflito(ConflitoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponseDto("Conflito", e.getMessage()));
    }

    /* 400 — alguma anotação de validação do DTO reprovou o corpo da requisição.

       Junta todas as mensagens numa string só, para o front não precisar fazer duas
       chamadas descobrindo um erro por vez. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> validacao(MethodArgumentNotValidException e) {
        StringBuilder erros = new StringBuilder();
        e.getBindingResult().getFieldErrors().forEach(erro ->
                erros.append(erro.getDefaultMessage()).append("; ")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Dados invalidos", erros.toString().trim()));
    }

    /* 400 — a URL trouxe um tipo errado, por exemplo /usuarios/abc onde se espera número.

       Sem este tratamento o Spring devolveria 500, dando a impressão de que o servidor
       quebrou quando na verdade o pedido é que estava malformado. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDto> tipoInvalido(MethodArgumentTypeMismatchException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponseDto("Parametro invalido",
                        "O valor informado para '" + e.getName() + "' nao e valido."));
    }

    /* 500 — falha de banco.

       A apostila devolve e.getMessage() no corpo. Aqui a mensagem do Oracle vai para o
       log do servidor e o cliente recebe um texto genérico: o erro do banco costuma
       conter nome de tabela, de coluna e de constraint, e a própria documentação de
       segurança do SoulUp pede tratamento de erros que não exponha o interior do
       sistema. Quem está depurando lê o log; quem está no navegador, não. */
    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ErrorResponseDto> erroDeBanco(SQLException e) {
        log.error("Falha de acesso ao banco de dados", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponseDto("Erro interno",
                        "Nao foi possivel completar a operacao. Tente novamente."));
    }
}
