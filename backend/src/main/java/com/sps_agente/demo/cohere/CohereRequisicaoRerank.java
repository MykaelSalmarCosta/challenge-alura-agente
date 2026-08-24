package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * DTO de requisição para POST /v2/rerank da Cohere.
 */
public record CohereRequisicaoRerank(
        String model,
        String query,
        List<String> documents,
        @JsonProperty("top_n") int topN
) {

    public static CohereRequisicaoRerank of(String query, List<String> documents, int topN) {
        return new CohereRequisicaoRerank("rerank-v3.5", query, documents, topN);
    }
}
