package br.com.spotmydeal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/* DTO de entrada do POST /usuarios/{id}/resgates.

   A posição do usuário vem no corpo porque é ela que o servidor usa para conferir
   a regra de proximidade — o celular manda onde está, e quem decide se vale é a BO.

   O cupom e os pontos não entram aqui: o cupom é copiado da loja no momento do resgate
   e os pontos são fixos. */
public class ResgateFormDto {

    @NotNull(message = "O codigo da loja e obrigatorio")
    @Positive(message = "O codigo da loja deve ser maior que zero")
    private Integer lojaId;

    @NotNull(message = "A latitude e obrigatoria")
    @DecimalMin(value = "-90.0", message = "Latitude deve estar entre -90 e 90")
    @DecimalMax(value = "90.0", message = "Latitude deve estar entre -90 e 90")
    private Double latitude;

    @NotNull(message = "A longitude e obrigatoria")
    @DecimalMin(value = "-180.0", message = "Longitude deve estar entre -180 e 180")
    @DecimalMax(value = "180.0", message = "Longitude deve estar entre -180 e 180")
    private Double longitude;

    public ResgateFormDto() {
    }

    // Getters e Setters
    public Integer getLojaId() { return lojaId; }
    public void setLojaId(Integer lojaId) { this.lojaId = lojaId; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
