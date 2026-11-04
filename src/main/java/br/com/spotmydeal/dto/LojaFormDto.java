package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.Loja;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/* DTO de entrada da loja

   Latitude e longitude são Double (objeto) e não double (primitivo) de propósito:
   com o primitivo, um JSON que esqueça o campo chegaria como 0.0, que é uma coordenada
   válida no Golfo da Guiné. Com o objeto, o campo ausente vira null e o @NotNull
   devolve 400 em vez de cadastrar a loja no meio do oceano. */
public class LojaFormDto {

    @NotBlank(message = "O nome da loja e obrigatorio")
    @Size(min = 3, max = 80, message = "O nome deve ter entre 3 e 80 caracteres")
    private String nome;

    @NotNull(message = "A latitude e obrigatoria")
    @DecimalMin(value = "-90.0", message = "Latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "Latitude deve estar entre -90 e 90")
    private Double latitude;

    @NotNull(message = "A longitude e obrigatoria")
    @DecimalMin(value = "-180.0", message = "Longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "Longitude deve estar entre -180 e 180")
    private Double longitude;

    @NotBlank(message = "O cupom e obrigatorio")
    @Size(max = 40, message = "O cupom deve ter no maximo 40 caracteres")
    private String cupomNome;

    // Quando o front não manda, a loja nasce ativa — mesmo padrão do construtor do model
    private boolean ativa = true;

    public LojaFormDto() {
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCupomNome() { return cupomNome; }
    public void setCupomNome(String cupomNome) { this.cupomNome = cupomNome; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    // Converte o DTO recebido num objeto de domínio, pronto para a camada BO
    public Loja toModel() {
        return new Loja(0, nome, latitude, longitude, cupomNome, ativa);
    }
}
