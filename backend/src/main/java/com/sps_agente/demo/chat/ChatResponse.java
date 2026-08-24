package com.sps_agente.demo.chat;

/**
 * DTO de saída para POST /api/chat.
 */
public record ChatResponse(String resposta, int tokensConsumidos) {}
