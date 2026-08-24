package com.sps_agente.demo.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Tratamento centralizado de exceções.
 * Mapeia falhas da Cohere e de validação para respostas HTTP consistentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> handleValidacao(MethodArgumentNotValidException ex) {
        String detalhe = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Erro de validação");

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErroResponse.of(400, "Requisição inválida", detalhe));
    }

    @ExceptionHandler(HttpClientErrorException.TooManyRequests.class)
    public ResponseEntity<ErroResponse> handleRateLimit(HttpClientErrorException.TooManyRequests ex) {
        log.warn("Rate limit da Cohere atingido (429)");
        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(ErroResponse.of(429, "Limite de requisições",
                        "O serviço de IA está temporariamente sobrecarregado. Tente novamente em alguns segundos."));
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErroResponse> handleClientError(HttpClientErrorException ex) {
        log.error("Erro do cliente Cohere: {} — {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ErroResponse.of(502, "Erro no provedor de IA",
                        "A requisição ao serviço de IA falhou. Tente novamente."));
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErroResponse> handleServerError(HttpServerErrorException ex) {
        log.error("Erro do servidor Cohere: {} — {}", ex.getStatusCode(), ex.getResponseBodyAsString());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(ErroResponse.of(502, "Erro no provedor de IA",
                        "O serviço de IA está com problemas. Tente novamente mais tarde."));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErroResponse> handleTimeout(ResourceAccessException ex) {
        log.error("Timeout ou falha de conexão com a Cohere: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.GATEWAY_TIMEOUT)
                .body(ErroResponse.of(504, "Timeout",
                        "A requisição ao serviço de IA demorou demais. Tente novamente."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> handleGenerico(Exception ex) {
        log.error("Erro inesperado", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErroResponse.of(500, "Erro interno",
                        "Ocorreu um erro inesperado. Tente novamente."));
    }
}
