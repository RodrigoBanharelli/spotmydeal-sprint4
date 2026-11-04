package br.com.spotmydeal.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/* DTO de entrada do POST /usuarios/{id}/missoes.

   Só carrega qual missão foi concluída. O usuário vem na URL, e a data e os pontos
   são decididos pelo servidor: deixar o front enviar "pontosGanhos" seria o mesmo
   que deixar ele escolher quanto quer ganhar. */
public class ConclusaoMissaoFormDto {

    @NotNull(message = "O codigo da missao e obrigatorio")
    @Positive(message = "O codigo da missao deve ser maior que zero")
    private Integer missaoId;

    public ConclusaoMissaoFormDto() {
    }

    public Integer getMissaoId() { return missaoId; }
    public void setMissaoId(Integer missaoId) { this.missaoId = missaoId; }
}
