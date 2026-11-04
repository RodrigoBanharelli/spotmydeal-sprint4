package br.com.spotmydeal.model;

public class Usuario {

    // ID do usuário e Dados pessoais do usuário
    private int id;
    private String nome;
    private String email;

    // Pontuação acumulada e nível de engajamento sustentável do usuário
    private int pontuacao;
    private String nivel;

    // Controla quantos dias se passaram desde a última missão concluída
    // Começa em 999 para indicar que nenhuma missão foi feita ainda
    private int diasDesdeUltimaMissao;

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON do POST
    public Usuario() {
        this.pontuacao = 0;
        this.nivel = "Iniciante";
        this.diasDesdeUltimaMissao = 999;
    }

    // Construtor — cria o usuário "default"
    public Usuario(int id, String nome, String email) {
        this();
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    // Construtor de cadastro — o id ainda não existe, será gerado pela sequence do banco no UsuarioDao
    public Usuario(String nome, String email) {
        this(0, nome, email);
    }

    // Construtor completo — usado pelo UsuarioDao para reconstruir o usuário a partir de uma linha da tabela
    public Usuario(int id, String nome, String email, int pontuacao, String nivel, int diasDesdeUltimaMissao) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.pontuacao = pontuacao;
        this.nivel = nivel;
        this.diasDesdeUltimaMissao = diasDesdeUltimaMissao;
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

    public int getDiasDesdeUltimaMissao() { return diasDesdeUltimaMissao; }
    public void setDiasDesdeUltimaMissao(int dias) { this.diasDesdeUltimaMissao = dias; }

    // Método de negócio: adiciona pontos ao usuário e recalcula o nível automaticamente
    public void adicionarPontos(int pontos) {
        this.pontuacao += pontos;
        atualizarNivel(); // Recalcula o nivel apos ganhar pontos(sem exeção)
    }

    // Atualiza o nível do usuário com base na pontuação acumulada
    private void atualizarNivel() {
        if (pontuacao >= 1000) {
            nivel = "Lenda Verde";
        } else if (pontuacao >= 800) {
            nivel = "Sustentavel Avancado";
        } else if (pontuacao >= 500) {
            nivel = "Ecologista";
        } else {
            nivel = "Iniciante";
        }
    }

    // Método de negócio: calcula quantos pontos faltam para o usuário subir de nível
    // Retorna 0 quando o usuário já está no nível máximo (Lenda Verde)
    public int pontosParaProximoNivel() {
        if (pontuacao >= 1000) {
            return 0;
        } else if (pontuacao >= 800) {
            return 1000 - pontuacao;
        } else if (pontuacao >= 500) {
            return 800 - pontuacao;
        } else {
            return 500 - pontuacao;
        }
    }

    // Retorna um resumo do usuário para exibição no ranking
    @Override
    public String toString() {
        return "Usuario: " + nome + " | Nivel: " + nivel + " | Pontuacao: " + pontuacao + " pts";
    }
}
