package br.com.spotmydeal.model;

import java.time.LocalDate;

/* Registro de que um usuário resgatou o cupom de uma loja — espelha a tabela T_SMD_RESGATE.

   Esta classe também não existia na Sprint 3. O processarResgate antigo só imprimia
   "OFERTA ATIVADA" no console e somava pontos; nada ficava gravado, então não havia
   histórico nem como o front mostrar os cupons do usuário.

   O cupom é copiado da loja no momento do resgate, de propósito: se a loja trocar a
   oferta depois, o histórico continua mostrando o que o usuário de fato resgatou. */
public class Resgate {

    // Código do resgate, gerado pela sequence do banco
    private int id;

    // Lados do relacionamento: quem resgatou e em qual loja
    private int usuarioId;
    private int lojaId;

    // Qual cupom foi resgatado, quando, e quantos pontos o resgate rendeu
    private String cupom;
    private LocalDate dataResgate;
    private int pontosGanhos;

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON
    public Resgate() {
    }

    // Construtor de cadastro — o id ainda não existe, será gerado pela sequence no ResgateDao
    public Resgate(int usuarioId, int lojaId, String cupom, LocalDate dataResgate, int pontosGanhos) {
        this(0, usuarioId, lojaId, cupom, dataResgate, pontosGanhos);
    }

    // Construtor completo — usado pelo ResgateDao para reconstruir a partir de uma linha da tabela
    public Resgate(int id, int usuarioId, int lojaId, String cupom, LocalDate dataResgate, int pontosGanhos) {
        this.id = id;
        this.usuarioId = usuarioId;
        this.lojaId = lojaId;
        this.cupom = cupom;
        this.dataResgate = dataResgate;
        this.pontosGanhos = pontosGanhos;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public int getLojaId() { return lojaId; }
    public void setLojaId(int lojaId) { this.lojaId = lojaId; }

    public String getCupom() { return cupom; }
    public void setCupom(String cupom) { this.cupom = cupom; }

    public LocalDate getDataResgate() { return dataResgate; }
    public void setDataResgate(LocalDate dataResgate) { this.dataResgate = dataResgate; }

    public int getPontosGanhos() { return pontosGanhos; }
    public void setPontosGanhos(int pontosGanhos) { this.pontosGanhos = pontosGanhos; }

    @Override
    public String toString() {
        return "Cupom " + cupom + " resgatado em " + dataResgate + " | +" + pontosGanhos + " pts";
    }
}
