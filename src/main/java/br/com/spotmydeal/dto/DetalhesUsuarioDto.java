package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.Usuario;

/* DTO de saída do usuário — o que a API devolve em JSON.
   Além dos dados guardados, devolve pontosParaProximoNivel, que não existe como coluna
   no banco: é calculado pelo método de negócio do próprio model. */
public class DetalhesUsuarioDto {

    private int id;
    private String nome;
    private String email;
    private int pontuacao;
    private String nivel;
    private int pontosParaProximoNivel;

    public DetalhesUsuarioDto() {
    }

    // Fábrica estática: monta o DTO a partir do objeto de domínio
    public static DetalhesUsuarioDto de(Usuario usuario) {
        DetalhesUsuarioDto dto = new DetalhesUsuarioDto();
        dto.id = usuario.getId();
        dto.nome = usuario.getNome();
        dto.email = usuario.getEmail();
        dto.pontuacao = usuario.getPontuacao();
        dto.nivel = usuario.getNivel();
        dto.pontosParaProximoNivel = usuario.pontosParaProximoNivel();
        return dto;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getPontuacao() { return pontuacao; }
    public void setPontuacao(int pontuacao) { this.pontuacao = pontuacao; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public int getPontosParaProximoNivel() { return pontosParaProximoNivel; }
    public void setPontosParaProximoNivel(int pontos) { this.pontosParaProximoNivel = pontos; }
}
