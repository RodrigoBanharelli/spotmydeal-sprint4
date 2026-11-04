package br.com.spotmydeal.exception;

/* Formato único de erro da API.

   Sem isso, cada tipo de falha devolveria um corpo diferente — o Spring tem um padrão
   próprio, o Oracle tem outro — e o front precisaria adivinhar onde está a mensagem.
   Com uma classe só, qualquer erro chega como {"erro": "...", "detalhe": "..."}.

   Apostila 17, p.27. */
public class ErrorResponseDto {

    // Categoria do erro, legível para quem desenvolve o front
    private String erro;

    // Mensagem específica do que aconteceu, vinda da exceção
    private String detalhe;

    public ErrorResponseDto() {
    }

    public ErrorResponseDto(String erro, String detalhe) {
        this.erro = erro;
        this.detalhe = detalhe;
    }

    // Getters e Setters
    public String getErro() { return erro; }
    public void setErro(String erro) { this.erro = erro; }

    public String getDetalhe() { return detalhe; }
    public void setDetalhe(String detalhe) { this.detalhe = detalhe; }
}
