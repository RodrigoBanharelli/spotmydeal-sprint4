package br.com.spotmydeal.exception;

/* Exceção lançada quando o usuário existe e a loja existe, mas ele não tem direito
   ao cupom neste momento.

   São as duas regras do resgate que vêm da Sprint 2: estar dentro do raio de ativação
   da loja e ter concluído alguma missão nos últimos 7 dias.

   Fica separada da RegraNegocioException de propósito: aqui o pedido está correto,
   o que falta é permissão. Por isso o ApiExceptionHandler traduz esta exceção para
   o status HTTP 403 Forbidden, e não para 400. */
public class ResgateBloqueadoException extends Exception {

    public ResgateBloqueadoException(String message) {
        super(message);
    }
}
