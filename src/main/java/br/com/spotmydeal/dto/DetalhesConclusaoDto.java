package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.UsuarioMissao;

import java.time.LocalDate;

/* DTO de saída de uma conclusão de missão — a linha de T_SMD_USUARIO_MISSAO em JSON.

   Devolve missaoId e não o título da missão: o título exigiria um join no DAO, e o
   front já carrega o catálogo de missões pelo GET /missoes. Se a tela de histórico
   ficar pesada por causa disso, o caminho é acrescentar o join no UsuarioMissaoDao. */
public class DetalhesConclusaoDto {

    private int id;
    private int usuarioId;
    private int missaoId;
    private LocalDate dataConclusao;
    private int pontosGanhos;

    public DetalhesConclusaoDto() {
    }

    // Fábrica estática: monta o DTO a partir do objeto de domínio
    public static DetalhesConclusaoDto de(UsuarioMissao conclusao) {
        DetalhesConclusaoDto dto = new DetalhesConclusaoDto();
        dto.id = conclusao.getId();
        dto.usuarioId = conclusao.getUsuarioId();
        dto.missaoId = conclusao.getMissaoId();
        dto.dataConclusao = conclusao.getDataConclusao();
        dto.pontosGanhos = conclusao.getPontosGanhos();
        return dto;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getMissaoId() { return missaoId; }
    public void setMissaoId(int missaoId) { this.missaoId = missaoId; }

    public LocalDate getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDate dataConclusao) { this.dataConclusao = dataConclusao; }

    public int getPontosGanhos() { return pontosGanhos; }
    public void setPontosGanhos(int pontosGanhos) { this.pontosGanhos = pontosGanhos; }
}
