package com.sps_agente.demo.rag;

import com.sps_agente.demo.cohere.CohereClient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class IndiceVetorial {

    private final CohereClient cohere;
    private final List<TrechoComMetadado> trechos = new ArrayList<>();
    private final List<double[]> vetores = new ArrayList<>();

    public IndiceVetorial(CohereClient cohere) {
        this.cohere = cohere;
    }

    /**
     * Indexa trechos associando-os ao nome do documento de origem.
     */
    public void indexar(List<String> novosTrechos, String nomeDocumento) {
        if (novosTrechos.isEmpty()) return;

        int tamanhoDeLote = 96;
        for (int i = 0; i < novosTrechos.size(); i += tamanhoDeLote) {
            List<String> lote = novosTrechos.subList(i, Math.min(i + tamanhoDeLote, novosTrechos.size()));
            var resposta = cohere.embedDocumentos(lote);
            var vetoresDoLote = resposta.vetores();

            for (String texto : lote) {
                trechos.add(new TrechoComMetadado(texto, nomeDocumento));
            }
            vetores.addAll(vetoresDoLote);
        }

        System.out.printf("[IndiceVetorial] %d trechos indexados (%d dimensões)%n",
                trechos.size(),
                vetores.isEmpty() ? 0 : vetores.getFirst().length);
    }

    /**
     * Busca os top-K trechos mais similares à pergunta por cosseno.
     * Retorna trechos com metadados para uso no reranker e na montagem de contexto.
     */
    public List<TrechoComMetadado> buscar(String pergunta, int topK) {
        var respostaEmbed = cohere.embedPergunta(pergunta);
        var vetoresPergunta = respostaEmbed.vetores();
        if (vetoresPergunta.isEmpty()) return List.of();

        double[] vetorPergunta = vetoresPergunta.getFirst();

        record Pontuado(int indice, double similaridade) {}

        List<Pontuado> pontuados = new ArrayList<>();
        for (int i = 0; i < trechos.size(); i++) {
            double sim = cossenoSimilaridade(vetorPergunta, vetores.get(i));
            pontuados.add(new Pontuado(i, sim));
        }

        return pontuados.stream()
                .sorted(Comparator.comparingDouble(Pontuado::similaridade).reversed())
                .limit(topK)
                .map(p -> trechos.get(p.indice()))
                .toList();
    }

    static double cossenoSimilaridade(double[] a, double[] b) {
        double produtoEscalar = 0.0;
        double normaA = 0.0;
        double normaB = 0.0;

        for (int i = 0; i < a.length; i++) {
            produtoEscalar += a[i] * b[i];
            normaA += a[i] * a[i];
            normaB += b[i] * b[i];
        }

        double denominador = Math.sqrt(normaA) * Math.sqrt(normaB);
        if (denominador == 0.0) return 0.0;
        return produtoEscalar / denominador;
    }

    public int tamanho() {
        return trechos.size();
    }

    public boolean vazio() {
        return trechos.isEmpty();
    }
}
