package com.sps_agente.demo.chat;

import com.sps_agente.demo.cohere.CohereClient;
import com.sps_agente.demo.cohere.CohereRespostaChat;
import com.sps_agente.demo.cohere.CohereRespostaEmbed;
import com.sps_agente.demo.cohere.CohereRespostaRerank;
import com.sps_agente.demo.cohere.Mensagem;
import com.sps_agente.demo.rag.IndiceVetorial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testa AgenteService com um CohereClient falso.
 * Nenhuma chamada real à API da Cohere.
 */
class AgenteServiceTest {

    private FakeCohereClient fakeClient;
    private AgenteService service;

    @BeforeEach
    void setUp() {
        fakeClient = new FakeCohereClient();
        IndiceVetorial indice = new IndiceVetorial(fakeClient);
        service = new AgenteService(fakeClient, indice);
    }

    @Test
    void deveResponderPerguntaSemDocumentosIndexados() {
        fakeClient.respostaChat = criarResposta("Olá! Sou o assistente da SPS.");

        ChatRequest request = new ChatRequest("Oi, tudo bem?", List.of());
        ChatResponse response = service.responder(request);

        assertNotNull(response);
        assertEquals("Olá! Sou o assistente da SPS.", response.resposta());
    }

    @Test
    void deveIncluirHistoricoNaRequisicao() {
        fakeClient.respostaChat = criarResposta("Sobre microsserviços...");

        var historico = List.of(
                new ChatRequest.MensagemDto("user", "O que é DDD?"),
                new ChatRequest.MensagemDto("assistant", "DDD é Domain-Driven Design.")
        );
        ChatRequest request = new ChatRequest("E microsserviços?", historico);
        ChatResponse response = service.responder(request);

        assertNotNull(response);
        assertTrue(fakeClient.ultimasMensagens.size() >= 4);
    }

    @Test
    void deveRetornarTokensConsumidos() {
        fakeClient.respostaChat = criarResposta("Resposta.", 150);

        ChatRequest request = new ChatRequest("Teste", List.of());
        ChatResponse response = service.responder(request);

        assertEquals(150, response.tokensConsumidos());
    }

    private CohereRespostaChat criarResposta(String texto) {
        return criarResposta(texto, 42);
    }

    private CohereRespostaChat criarResposta(String texto, int tokens) {
        var content = new CohereRespostaChat.ContentBlock("text", texto);
        var message = new CohereRespostaChat.Message("assistant", List.of(content));
        int input = tokens / 2;
        int output = tokens - input;
        var usage = new CohereRespostaChat.Usage(new CohereRespostaChat.Tokens(input, output));
        return new CohereRespostaChat("fake-id", message, usage);
    }

    /**
     * Implementação falsa do CohereClient para testes.
     */
    static class FakeCohereClient extends CohereClient {

        CohereRespostaChat respostaChat;
        List<Mensagem> ultimasMensagens;

        FakeCohereClient() {
            super("fake-key-for-test");
        }

        @Override
        public CohereRespostaChat chat(List<Mensagem> mensagens) {
            this.ultimasMensagens = mensagens;
            return respostaChat;
        }

        @Override
        public CohereRespostaEmbed embedDocumentos(List<String> textos) {
            return respostaEmbedVazia();
        }

        @Override
        public CohereRespostaEmbed embedPergunta(String pergunta) {
            return respostaEmbedVazia();
        }

        @Override
        public CohereRespostaRerank rerank(String pergunta, List<String> documentos, int topN) {
            var resultados = new java.util.ArrayList<CohereRespostaRerank.Resultado>();
            for (int i = 0; i < Math.min(topN, documentos.size()); i++) {
                resultados.add(new CohereRespostaRerank.Resultado(i, 1.0 - i * 0.1));
            }
            return new CohereRespostaRerank(resultados);
        }

        private CohereRespostaEmbed respostaEmbedVazia() {
            return new CohereRespostaEmbed(new CohereRespostaEmbed.Embeddings(List.of()));
        }
    }
}
