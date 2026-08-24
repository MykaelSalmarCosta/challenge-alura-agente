package com.sps_agente.demo.rag;

/**
 * Um trecho de texto com o nome do documento de origem.
 * Usado para preservar a rastreabilidade na montagem do contexto.
 */
public record TrechoComMetadado(String texto, String documento) {}
