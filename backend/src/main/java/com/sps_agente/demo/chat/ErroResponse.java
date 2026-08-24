package com.sps_agente.demo.chat;

import java.time.Instant;

/**
 * DTO padronizado de erro para todas as respostas de falha da API.
 */
public record ErroResponse(
        int status,
        String erro,
        String mensagem,
        Instant timestamp
) {
    public static ErroResponse of(int status, String erro, String mensagem) {
        return new ErroResponse(status, erro, mensagem, Instant.now());
    }
}
