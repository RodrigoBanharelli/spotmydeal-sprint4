package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.Resgate;

import java.time.LocalDate;

// DTO de saída de um resgate — a linha de T_SMD_RESGATE em JSON
public class DetalhesResgateDto {

    private int id;
    private int usuarioId;
    private int lojaId;
    private String cupom;
    private LocalDate dataResgate;
    private int pontosGanhos;

    public DetalhesResgateDto() {
    }

    // Fábrica estática: monta o DTO a partir do objeto de domínio
    public static DetalhesResgateDto de(Resgate resgate) {
        DetalhesResgateDto dto = new DetalhesResgateDto();
        dto.id = resgate.getId();
        dto.usuarioId = resgate.getUsuarioId();
        dto.lojaId = resgate.getLojaId();
        dto.cupom = resgate.getCupom();
        dto.dataResgate = resgate.getDataResgate();
        dto.pontosGanhos = resgate.getPontosGanhos();
        return dto;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getLojaId() { return lojaId; }
    public void setLojaId(int lojaId) { this.lojaId = lojaId; }

    public String getCupom() { return cupom; }
    public void setCupom(String cupom) { this.cupom = cupom; }

    public LocalDate getDataResgate() { return dataResgate; }
    public void setDataResgate(LocalDate dataResgate) { this.dataResgate = dataResgate; }

    public int getPontosGanhos() { return pontosGanhos; }
    public void setPontosGanhos(int pontosGanhos) { this.pontosGanhos = pontosGanhos; }
}
