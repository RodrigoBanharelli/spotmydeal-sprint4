package br.com.spotmydeal.model;

import java.time.LocalDate;

/* Registro de que um usuário concluiu uma missão — espelha a tabela T_SMD_USUARIO_MISSAO.

   Esta classe não existia na Sprint 3, e foi exatamente o que o feedback apontou como
   faltando: sem ela, a conclusão ficava num boolean dentro da própria missão e valia
   para todos os usuários ao mesmo tempo.

   O par (usuarioId, missaoId) é único no banco, então a mesma missão não pode ser
   concluída duas vezes pelo mesmo usuário. */
public class UsuarioMissao {

    // Código do registro, gerado pela sequence do banco
    private int id;

    // Lados do relacionamento: quem concluiu e qual missão
    private int usuarioId;
    private int missaoId;

    // Quando concluiu e quantos pontos essa conclusão rendeu
    // Os pontos ficam gravados aqui porque a missão pode ter o valor alterado depois
    private LocalDate dataConclusao;
    private int pontosGanhos;

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON
    public UsuarioMissao() {
    }

    // Construtor de cadastro — o id ainda não existe, será gerado pela sequence no UsuarioMissaoDao
    public UsuarioMissao(int usuarioId, int missaoId, LocalDate dataConclusao, int pontosGanhos) {
        this(0, usuarioId, missaoId, dataConclusao, pontosGanhos);
    }

    // Construtor completo — usado pelo UsuarioMissaoDao para reconstruir a partir de uma linha da tabela
    public UsuarioMissao(int id, int usuarioId, int missaoId, LocalDate dataConclusao, int pontosGanhos) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.missaoId = missaoId;
        this.dataConclusao = dataConclusao;
        this.pontosGanhos = pontosGanhos;
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

    // Método de negócio: indica se a conclusão aconteceu dentro da janela de dias informada
    // É o que a regra do resgate usa para saber se o usuário andou ativo (7 dias)
    public boolean concluidaNosUltimos(int dias) {
        if (dataConclusao == null) {
            return false;
        }
        return !dataConclusao.isBefore(LocalDate.now().minusDays(dias));
    }

    @Override
    public String toString() {
        return "Missao " + missaoId + " concluida em " + dataConclusao + " | +" + pontosGanhos + " pts";
    }
}
