package br.com.spotmydeal.exception;

// Exceção de negócio lançada pelo DAO quando o registro procurado não existe no banco
// Separa o "não encontrei" (regra) do "deu erro no banco" (SQLException)
public class EntidadeNaoEncontradaException extends Exception {

    public EntidadeNaoEncontradaException(String message) {
        super(message);
    }
}
