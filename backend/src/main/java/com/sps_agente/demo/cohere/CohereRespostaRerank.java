package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de resposta de POST /v2/rerank da Cohere.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CohereRespostaRerank(List<Resultado> results) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Resultado(
            int index,
            @JsonProperty("relevance_score") double relevanceScore
    ) {}

    /**
     * Retorna os índices dos documentos ordenados por relevância (maior primeiro).
     */
    public List<Integer> indicesOrdenados() {
        if (results == null) return List.of();
        return results.stream()
                .sorted((a, b) -> Double.compare(b.relevanceScore(), a.relevanceScore()))
                .map(Resultado::index)
                .toList();
    }
}
