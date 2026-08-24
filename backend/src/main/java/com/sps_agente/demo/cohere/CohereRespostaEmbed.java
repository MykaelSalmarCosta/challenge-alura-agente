package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CohereRespostaEmbed(Embeddings embeddings) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Embeddings(@JsonProperty("float") List<List<Double>> floatVectors) {
    }

    public List<double[]> vetores() {
        if (embeddings == null || embeddings.floatVectors() == null) return List.of();
        return embeddings.floatVectors().stream()
                .map(lista -> lista.stream().mapToDouble(Double::doubleValue).toArray())
                .toList();
    }
}
