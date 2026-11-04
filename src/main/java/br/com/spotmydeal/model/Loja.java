package br.com.spotmydeal.model;

// Representa uma loja parceira cadastrada no sistema

public class Loja {

    /* Informações da loja:

    -Código da loja, gerado pela sequence do banco
    -Nome da loja
    -Coordenadas GPS da loja para utilizar na proximidade(LA/LO)
    -Código do cupom da loja
    -Indica se a loja está ativa e se pode estar ativa
     */
    private int id;
    private String nome;
    private double latitude;
    private double longitude;
    private String cupomNome;
    private boolean ativa;

    // Construtor vazio — exigido pelo Jackson para montar o objeto a partir do JSON do POST
    public Loja() {
        this.ativa = true;
    }

    // Construtor de cadastro — o id ainda não existe, será gerado pela sequence do banco no LojaDao
    public Loja(String nome, double latitude, double longitude, String cupomNome) {
        this(0, nome, latitude, longitude, cupomNome, true);
    }

    // Construtor completo — usado pelo LojaDao para reconstruir a loja a partir de uma linha da tabela
    public Loja(int id, String nome, double latitude, double longitude, String cupomNome, boolean ativa) {
        this.id = id;
        this.nome = nome;
        this.latitude = latitude;
        this.longitude = longitude;
        this.cupomNome = cupomNome;
        this.ativa = ativa;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getCupomNome() { return cupomNome; }
    public void setCupomNome(String cupomNome) { this.cupomNome = cupomNome; }

    public boolean isAtiva() { return ativa; }
    public void setAtiva(boolean ativa) { this.ativa = ativa; }

    // Retorna uma descrição resumida da loja para exibir
    @Override
    public String toString() {
        return "Loja: " + nome + " | Cupom: " + cupomNome + " | Ativa: " + (ativa ? "Sim" : "Nao");
    }
}
