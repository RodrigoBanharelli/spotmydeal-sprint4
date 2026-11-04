package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.MissaoSustentabilidade;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/* DTO de entrada da missão — o que o front envia no POST e no PUT.

   Não tem campo "concluida", e não é esquecimento: conclusão é do par usuário x missão
   e se registra pelo POST /usuarios/{id}/missoes, não editando a missão. */
public class MissaoFormDto {

    @NotBlank(message = "O titulo da missao e obrigatorio")
    @Size(min = 3, max = 80, message = "O titulo deve ter entre 3 e 80 caracteres")
    private String titulo;

    @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
    private String descricao;

    // A constraint CK_SMD_MISSAO_PONTOS do banco também exige maior que zero;
    // validar aqui devolve 400 com mensagem clara em vez de deixar estourar no Oracle
    @NotNull(message = "Os pontos de recompensa sao obrigatorios")
    @Positive(message = "Os pontos de recompensa devem ser maiores que zero")
    private Integer pontosRecompensa;

    @NotNull(message = "O prazo em dias e obrigatorio")
    @Positive(message = "O prazo em dias deve ser maior que zero")
    private Integer diasLimite;

    public MissaoFormDto() {
    }

    // Getters e Setters
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public Integer getPontosRecompensa() { return pontosRecompensa; }
    public void setPontosRecompensa(Integer pontosRecompensa) { this.pontosRecompensa = pontosRecompensa; }

    public Integer getDiasLimite() { return diasLimite; }
    public void setDiasLimite(Integer diasLimite) { this.diasLimite = diasLimite; }

    // Converte o DTO recebido num objeto de domínio, pronto para a camada BO
    public MissaoSustentabilidade toModel() {
        return new MissaoSustentabilidade(titulo, descricao, pontosRecompensa, diasLimite);
    }
}
