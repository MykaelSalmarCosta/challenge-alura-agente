package com.sps_agente.demo.chat;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * DTO de entrada para POST /api/chat.
 * O cliente envia a pergunta atual e o histórico da conversa.
 */
public record ChatRequest(
        @NotBlank(message = "A pergunta não pode ser vazia")
        String pergunta,

        List<MensagemDto> historico
) {
    public ChatRequest {
        if (historico == null) historico = List.of();
    }

    public record MensagemDto(String role, String conteudo) {}
}
