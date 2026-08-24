package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
public record CohereRespostaChat(String id, Message message, Usage usage) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(String role, List<ContentBlock> content) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentBlock(String type, String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Usage(Tokens tokens) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Tokens(
            @JsonProperty("input_tokens") Integer inputTokens,
            @JsonProperty("output_tokens") Integer outputTokens) {
    }


    public String texto() {
        if (message == null || message.content() == null) return "";
        return message.content().stream()
                .filter(b -> "text".equals(b.type()))
                .map(ContentBlock::text)
                .findFirst()
                .orElse("");
    }


    public int tokensConsumidos() {
        if (usage == null || usage.tokens() == null) return 0;
        int in = usage.tokens().inputTokens() != null ? usage.tokens().inputTokens() : 0;
        int out = usage.tokens().outputTokens() != null ? usage.tokens().outputTokens() : 0;
        return in + out;
    }
}
