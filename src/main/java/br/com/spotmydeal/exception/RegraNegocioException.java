package br.com.spotmydeal.exception;

/* Exceção lançada quando os dados até existem, mas a operação fere uma regra do SoulUp.

   Exemplos: nome com menos de 3 letras, e-mail já cadastrado para outra pessoa,
   missão fora do prazo, loja desativada.

   O ApiExceptionHandler traduz esta exceção para o status HTTP 400 Bad Request. */
public class RegraNegocioException extends Exception {

    public RegraNegocioException(String message) {
        super(message);
    }
}
