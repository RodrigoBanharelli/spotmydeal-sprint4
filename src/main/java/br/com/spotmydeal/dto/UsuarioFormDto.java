package br.com.spotmydeal.dto;

import br.com.spotmydeal.model.Usuario;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/* DTO de entrada do usuário — o que o front envia no POST e no PUT.

   Serve aos dois verbos porque, neste domínio, cadastrar e alterar recebem exatamente
   os mesmos campos. A apostila cria um DTO por ação (CadastroAlunoDto e
   AtualizacaoAlunoDto); aqui seriam duas classes idênticas, então ficou uma só.
   Se um dia o cadastro passar a pedir algo que a alteração não pede, aí vale separar.*/
public class UsuarioFormDto {

    @NotBlank(message = "O nome e obrigatorio")
    @Size(min = 3, max = 80, message = "O nome deve ter entre 3 e 80 caracteres")
    private String nome;

    @NotBlank(message = "O e-mail e obrigatorio")
    @Email(message = "E-mail invalido")
    @Size(max = 120, message = "O e-mail deve ter no maximo 120 caracteres")
    private String email;

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON
    public UsuarioFormDto() {
    }

    public UsuarioFormDto(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // Converte o DTO recebido num objeto de domínio, pronto para a camada BO
    public Usuario toModel() {
        return new Usuario(nome, email);
    }
}
