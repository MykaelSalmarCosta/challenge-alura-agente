package com.sps_agente.demo.rag;

import java.util.Comparator;
import java.util.List;


public class BuscadorPorPalavraChave {

    
    public static List<String> buscar(String pergunta, List<String> trechos, int topK) {
        String[] termos = pergunta.toLowerCase().split("\\W+");

        record Pontuado(String trecho, long pontos) {}

        return trechos.stream()
                .map(trecho -> {
                    String lower = trecho.toLowerCase();
                    long pontos = 0;
                    for (String termo : termos) {
                        if (!termo.isBlank() && lower.contains(termo)) {
                            pontos++;
                        }
                    }
                    return new Pontuado(trecho, pontos);
                })
                .sorted(Comparator.comparingLong(Pontuado::pontos).reversed())
                .limit(topK)
                .filter(p -> p.pontos() > 0)
                .map(Pontuado::trecho)
                .toList();
    }
}
