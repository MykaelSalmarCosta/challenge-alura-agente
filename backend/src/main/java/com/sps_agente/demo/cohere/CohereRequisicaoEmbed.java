package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public record CohereRequisicaoEmbed(
        String model,
        List<String> texts,
        @JsonProperty("input_type") String inputType,
        @JsonProperty("embedding_types") List<String> embeddingTypes
) {

    public static CohereRequisicaoEmbed paraDocumentos(List<String> textos) {
        return new CohereRequisicaoEmbed(
                "embed-v4.0",
                textos,
                "search_document",
                List.of("float")
        );
    }

    public static CohereRequisicaoEmbed paraPergunta(String pergunta) {
        return new CohereRequisicaoEmbed(
                "embed-v4.0",
                List.of(pergunta),
                "search_query",
                List.of("float")
        );
    }
}
