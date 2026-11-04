package br.com.spotmydeal.exception;

// Exceção de negócio lançada pelo DAO quando o registro procurado não existe no banco
// Separa o "não encontrei" (regra) do "deu erro no banco" (SQLException)
// Na Sprint 4 é ela que o ApiExceptionHandler traduz para o status HTTP 404
public class EntidadeNaoEncontradaException extends Exception {

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
