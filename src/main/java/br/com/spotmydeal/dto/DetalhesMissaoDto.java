package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.MissaoSustentabilidade;

// DTO de saída da missão — o que a API devolve em JSON
public class DetalhesMissaoDto {

    private int id;
    private String titulo;
    private String descricao;
    private int pontosRecompensa;
    private int diasLimite;

    // Calculado pelo método de negócio do model: indica se a missão ainda está no prazo
    private boolean valida;

    public DetalhesMissaoDto() {
    }

    // Fábrica estática: monta o DTO a partir do objeto de domínio
    public static DetalhesMissaoDto de(MissaoSustentabilidade missao) {
        DetalhesMissaoDto dto = new DetalhesMissaoDto();
        dto.id = missao.getId();
        dto.titulo = missao.getTitulo();
        dto.descricao = missao.getDescricao();
        dto.pontosRecompensa = missao.getPontosRecompensa();
        dto.diasLimite = missao.getDiasLimite();
        dto.valida = missao.estaValida();
        return dto;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getPontosRecompensa() { return pontosRecompensa; }
    public void setPontosRecompensa(int pontosRecompensa) { this.pontosRecompensa = pontosRecompensa; }

    public int getDiasLimite() { return diasLimite; }
    public void setDiasLimite(int diasLimite) { this.diasLimite = diasLimite; }

    public boolean isValida() { return valida; }
    public void setValida(boolean valida) { this.valida = valida; }
}
