package com.sps_agente.demo.cohere;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;

/**
 * Cliente HTTP para a API da Cohere v2.
 * Encapsula autenticação, serialização, timeout e retry com recuo.
 */
@Component
public class CohereClient {

    private static final Logger log = LoggerFactory.getLogger(CohereClient.class);

    private static final String BASE_URL = "https://api.cohere.com/v2";
    private static final String MODELO_CHAT = "command-a-03-2025";
    private static final int MAX_TENTATIVAS = 3;
    private static final long RECUO_BASE_MS = 1000;

    private final RestClient restClient;

    public CohereClient(@Value("${cohere.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .requestFactory(clientHttpRequestFactory())
                .build();
    }

    private static org.springframework.http.client.ClientHttpRequestFactory clientHttpRequestFactory() {
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(60));
        return factory;
    }

    public CohereRespostaChat chat(List<Mensagem> mensagens) {
        var requisicao = new CohereRequisicaoChat(MODELO_CHAT, mensagens);
        return executarComRetry("/chat", requisicao, CohereRespostaChat.class);
    }

    public CohereRespostaEmbed embedDocumentos(List<String> textos) {
        var requisicao = CohereRequisicaoEmbed.paraDocumentos(textos);
        return executarComRetry("/embed", requisicao, CohereRespostaEmbed.class);
    }

    public CohereRespostaEmbed embedPergunta(String pergunta) {
        var requisicao = CohereRequisicaoEmbed.paraPergunta(pergunta);
        return executarComRetry("/embed", requisicao, CohereRespostaEmbed.class);
    }

    /**
     * Reranqueia documentos por relevância em relação à pergunta.
     */
    public CohereRespostaRerank rerank(String pergunta, List<String> documentos, int topN) {
        var requisicao = CohereRequisicaoRerank.of(pergunta, documentos, topN);
        return executarComRetry("/rerank", requisicao, CohereRespostaRerank.class);
    }

    private <T> T executarComRetry(String uri, Object body, Class<T> tipo) {
        for (int tentativa = 1; tentativa <= MAX_TENTATIVAS; tentativa++) {
            try {
                return restClient.post()
                        .uri(uri)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(tipo);
            } catch (HttpClientErrorException.TooManyRequests ex) {
                if (tentativa == MAX_TENTATIVAS) {
                    log.warn("Rate limit (429) após {} tentativas em {}. Desistindo.", tentativa, uri);
                    throw ex;
                }
                long espera = RECUO_BASE_MS * (1L << (tentativa - 1));
                log.info("Rate limit (429) em {}. Tentativa {}/{}. Aguardando {}ms...",
                        uri, tentativa, MAX_TENTATIVAS, espera);
                try {
                    Thread.sleep(espera);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw ex;
                }
            }
        }
        throw new IllegalStateException("Loop de retry encerrou sem resultado");
    }
}
