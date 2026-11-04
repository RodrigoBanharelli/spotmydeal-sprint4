package br.com.spotmydeal.bo;

import br.com.spotmydeal.model.Loja;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/* Regras de distância entre o usuário e as lojas parceiras.

   Era o GeolocalizacaoService da Sprint 3. O que mudou na Sprint 4:

   - virou @Service, então o Spring injeta nos Resources sem new;
   - saiu todo o System.out e o JOptionPane. O antigo exibirAviso() criava um JFrame
     para mostrar a loja mais próxima numa janela — numa API isso trava a thread da
     requisição esperando um clique que nunca vem, e quebra de vez no servidor do
     Render, que roda sem interface gráfica (JRE headless);
   - os métodos passaram a receber List<Loja> em vez de Loja[], porque é o que o
     LojaDao devolve.

   O cálculo em si — a fórmula de Haversine escrita na mão — está igual ao da Sprint 3. */
@Service
public class GeolocalizacaoBO {

    // Distância máxima em metros para considerar que o usuário está perto de uma loja
    private double raioAtivacao;

    // Construtor padrão — define raio de 200 metros
    public GeolocalizacaoBO() {
        this.raioAtivacao = 200.0;
    }

    // Getters e Setters
    public double getRaioAtivacao() { return raioAtivacao; }
    public void setRaioAtivacao(double raioAtivacao) { this.raioAtivacao = raioAtivacao; }

    // Verifica se o usuário está dentro do raio de ativação da loja
    public boolean verificarProximidade(double userLat, double userLon, Loja loja) {
        double distancia = calcularDistanciaMetros(userLat, userLon, loja.getLatitude(), loja.getLongitude());
        return distancia <= raioAtivacao;
    }

    /*Método1 geo: calcula a distância real em metros entre dois pontos GPS
     usa a fórmula de Haversine, que leva em conta a curvatura da Terra*/
    public double calcularDistanciaMetros(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // raio médio da Terra em quilômetros

        // Converte a diferença de graus para radianos
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // Fórmula de Haversine
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c * 1000; // converte de km para metros antes de retornar
    }

    /* Método2 geo: percorre as lojas e retorna a mais próxima do usuário.

       Devolve null quando a lista vem vazia — quem chama decide o que fazer com isso.
       Na Sprint 3 este método imprimia a resposta e abria um popup; agora ele só calcula. */
    public Loja encontrarMaisProxima(double userLat, double userLon, List<Loja> lojas) {
        Loja maisProxima = null;
        double menorDistancia = Double.MAX_VALUE; // começa com o maior valor possível

        for (Loja loja : lojas) {
            double distancia = calcularDistanciaMetros(userLat, userLon,
                    loja.getLatitude(), loja.getLongitude());

            // Atualiza a loja mais próxima se encontrar uma distância menor
            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                maisProxima = loja;
            }
        }

        return maisProxima;
    }

    /* Método3 geo: devolve as lojas dentro de um raio, da mais perto para a mais longe.

       É o que responde o GET /lojas/proximas. A ordenação é um selection sort escrito
       na mão sobre as distâncias já calculadas: assim cada distância é calculada uma
       única vez, em vez de recalcular a cada comparação da ordenação. */
    public List<Loja> filtrarPorRaio(double userLat, double userLon, double raio, List<Loja> lojas) {
        List<Loja> dentroDoRaio = new ArrayList<>();
        List<Double> distancias = new ArrayList<>();

        // Primeiro passo: separa quem está dentro do raio, guardando a distância de cada um
        for (Loja loja : lojas) {
            double distancia = calcularDistanciaMetros(userLat, userLon,
                    loja.getLatitude(), loja.getLongitude());
            if (distancia <= raio) {
                dentroDoRaio.add(loja);
                distancias.add(distancia);
            }
        }

        // Segundo passo: selection sort — a cada volta, procura a menor distância que
        // sobrou e troca para a posição atual, movendo as duas listas em paralelo
        for (int i = 0; i < dentroDoRaio.size() - 1; i++) {
            int indiceMenor = i;
            for (int j = i + 1; j < dentroDoRaio.size(); j++) {
                if (distancias.get(j) < distancias.get(indiceMenor)) {
                    indiceMenor = j;
                }
            }

            if (indiceMenor != i) {
                Loja lojaTemp = dentroDoRaio.get(i);
                dentroDoRaio.set(i, dentroDoRaio.get(indiceMenor));
                dentroDoRaio.set(indiceMenor, lojaTemp);

                Double distanciaTemp = distancias.get(i);
                distancias.set(i, distancias.get(indiceMenor));
                distancias.set(indiceMenor, distanciaTemp);
            }
        }

        return dentroDoRaio;
    }
}
