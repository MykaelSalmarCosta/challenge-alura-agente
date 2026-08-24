package com.sps_agente.demo.cohere;

import java.util.List;


public record CohereRequisicaoChat(String model, List<Mensagem> messages) {
}
