package com.sps_agente.demo.cohere;

import com.fasterxml.jackson.annotation.JsonInclude;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record Mensagem(String role, String content) {

    public static Mensagem usuario(String texto) {
        return new Mensagem("user", texto);
    }

    public static Mensagem assistente(String texto) {
        return new Mensagem("assistant", texto);
    }

    public static Mensagem sistema(String texto) {
        return new Mensagem("system", texto);
    }
}
