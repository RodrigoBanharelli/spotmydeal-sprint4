package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.Loja;

// DTO de saída da loja — o que a API devolve em JSON
public class DetalhesLojaDto {

    private int id;
    private String nome;
    private double latitude;
    private double longitude;
    private String cupomNome;
    private boolean ativa;

    public DetalhesLojaDto() {
    }

    // Fábrica estática: monta o DTO a partir do objeto de domínio
    public static DetalhesLojaDto de(Loja loja) {
        DetalhesLojaDto dto = new DetalhesLojaDto();
        dto.id = loja.getId();
        dto.nome = loja.getNome();
        dto.latitude = loja.getLatitude();
        dto.longitude = loja.getLongitude();
        dto.cupomNome = loja.getCupomNome();
        dto.ativa = loja.isAtiva();
        return dto;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCupomNome() { return cupomNome; }
    public void setCupomNome(String cupomNome) { this.cupomNome = cupomNome; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }
}
