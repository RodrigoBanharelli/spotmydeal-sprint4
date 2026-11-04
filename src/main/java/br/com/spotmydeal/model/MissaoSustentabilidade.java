package br.com.spotmydeal.model;

import java.time.LocalDate;

// Representa uma missão de sustentabilidade que o usuário pode completar para ganhar pontos
public class MissaoSustentabilidade {

    // Código da missão, gerado pela sequence do banco
    private int id;

    // Nome da missão
    private String titulo;

    // Descriçao da missão
    private String descricao;

    // Quantidade de pontos concedida ao usuário depois de terminar a missao
    private int pontosRecompensa;

    // Prazo de dias para completar a missão
    private int diasLimite;

    /* O campo "concluida" existia aqui na Sprint 3 e saiu na Sprint 4.
       Ele era um erro de modelagem: a conclusão pertence ao par usuário x missão,
       não à missão. Com o boolean na missão, assim que um usuário concluísse,
       a missão ficaria concluída para todos os outros.
       Agora quem guarda isso é a tabela T_SMD_USUARIO_MISSAO, via a classe UsuarioMissao. */

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON do POST
    public MissaoSustentabilidade() {
    }

    // Construtor de cadastro — o id ainda não existe, será gerado pela sequence do banco no MissaoDao
    public MissaoSustentabilidade(String titulo, String descricao, int pontosRecompensa, int diasLimite) {
        this(0, titulo, descricao, pontosRecompensa, diasLimite);
    }

    // Construtor completo — usado pelo MissaoDao para reconstruir a missão a partir de uma linha da tabela
    public MissaoSustentabilidade(int id, String titulo, String descricao, int pontosRecompensa, int diasLimite) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.pontosRecompensa = pontosRecompensa;
        this.diasLimite = diasLimite;
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

    /* Método de negócio: aplica o efeito da missão no usuário e devolve o registro
       da conclusão, que a camada BO grava em T_SMD_USUARIO_MISSAO.
       Quem verifica se este usuário já concluiu a missão é o UsuarioMissaoDao —
       aqui a missão só sabe premiar. */
    public UsuarioMissao concluir(Usuario usuario) {
        // Adiciona os pontos da missão ao usuário (o nível é recalculado dentro)
        usuario.adicionarPontos(pontosRecompensa);

        // Zera o contador para que o usuário possa resgatar cupons em lojas próximas
        usuario.setDiasDesdeUltimaMissao(0);

        return new UsuarioMissao(usuario.getId(), this.id, LocalDate.now(), pontosRecompensa);
    }

    // Método de negócio: verifica se a missão ainda está dentro do prazo para ser realizada
    public boolean estaValida() {
        return diasLimite > 0;
    }

    // Resumo da missão para exibição no menu
    @Override
    public String toString() {
        return "Missao: " + titulo + " | Pontos: " + pontosRecompensa + " | Valida: " + (estaValida() ? "Sim" : "Nao");
    }
}
