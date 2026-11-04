package br.com.spotmydeal.exception;

/* Exceção lançada quando a operação não pode ser repetida porque já foi feita antes.

   O caso principal é o usuário tentar concluir uma missão que ele já concluiu.
   Sem esta exceção, a tentativa chegaria até o banco e estouraria na constraint
   UK_SMD_UM_PAR, virando um erro 500 sem explicação para o front.

   O ApiExceptionHandler traduz esta exceção para o status HTTP 409 Conflict. */
public class ConflitoException extends Exception {

    public ConflitoException(String message) {
        super(message);
    }
}
