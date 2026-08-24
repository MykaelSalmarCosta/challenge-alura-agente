package com.sps_agente.demo.chat;

import com.sps_agente.demo.cohere.CohereClient;
import com.sps_agente.demo.cohere.CohereRespostaChat;
import com.sps_agente.demo.cohere.CohereRespostaRerank;
import com.sps_agente.demo.cohere.Mensagem;
import com.sps_agente.demo.rag.IndiceVetorial;
import com.sps_agente.demo.rag.TrechoComMetadado;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de negócio do agente: monta o contexto RAG com reranqueamento e chama a Cohere.
 */
@Service
public class AgenteService {

    private static final Logger log = LoggerFactory.getLogger(AgenteService.class);

    /** Candidatos amplos da busca vetorial — alimentam o reranker. */
    private static final int CANDIDATOS_BUSCA = 20;

    /** Finalistas após reranqueamento — vão para o contexto do LLM. */
    private static final int TOP_K_FINAL = 5;

    private final CohereClient cohere;
    private final IndiceVetorial indice;

    public AgenteService(CohereClient cohere, IndiceVetorial indice) {
        this.cohere = cohere;
        this.indice = indice;
    }

    public ChatResponse responder(ChatRequest request) {
        List<Mensagem> mensagens = new ArrayList<>();

        mensagens.add(Mensagem.sistema(instrucaoDoSistema()));

        // 1. Busca vetorial ampla
        List<TrechoComMetadado> candidatos = indice.buscar(request.pergunta(), CANDIDATOS_BUSCA);

        // 2. Reranqueamento (se houver candidatos suficientes)
        List<TrechoComMetadado> finalistas = reranquear(request.pergunta(), candidatos);

        // 3. Montagem do contexto com metadados
        if (!finalistas.isEmpty()) {
            mensagens.add(Mensagem.sistema(montarContexto(finalistas)));
        }

        for (var msg : request.historico()) {
            mensagens.add(new Mensagem(msg.role(), msg.conteudo()));
        }

        mensagens.add(Mensagem.usuario(request.pergunta()));

        CohereRespostaChat resposta = cohere.chat(mensagens);

        log.info("Tokens consumidos: {} (pergunta: \"{}\")",
                resposta.tokensConsumidos(),
                request.pergunta().length() > 80
                        ? request.pergunta().substring(0, 80) + "..."
                        : request.pergunta());

        return new ChatResponse(resposta.texto(), resposta.tokensConsumidos());
    }

    /**
     * Reranqueia os candidatos usando o endpoint /v2/rerank da Cohere.
     * Se houver poucos candidatos, retorna todos sem chamar o reranker.
     */
    private List<TrechoComMetadado> reranquear(String pergunta, List<TrechoComMetadado> candidatos) {
        if (candidatos.isEmpty()) return List.of();

        // Com poucos candidatos, não vale a chamada extra ao reranker
        if (candidatos.size() <= TOP_K_FINAL) return candidatos;

        List<String> textos = candidatos.stream().map(TrechoComMetadado::texto).toList();

        CohereRespostaRerank resposta = cohere.rerank(pergunta, textos, TOP_K_FINAL);

        return resposta.indicesOrdenados().stream()
                .map(candidatos::get)
                .toList();
    }

    private String instrucaoDoSistema() {
        return """
                Você é um assistente simpático e prestativo da Santo Pegasus Soluciones, especializado nos documentos internos da empresa.

                ## Comportamento geral
                - Responda saudações e cumprimentos de forma natural e acolhedora. Apresente-se brevemente e diga que está ali para ajudar com dúvidas sobre os documentos da empresa.
                - Para perguntas sobre conteúdo, responda EXCLUSIVAMENTE com base nos trechos de documento fornecidos.
                - Nunca invente, extrapole ou use conhecimento externo aos documentos.
                - Se o usuário fizer perguntas completamente fora do escopo (ex: clima, esportes, programação geral), informe educadamente que seu foco são os documentos internos da empresa.
                - Use um tom profissional mas amigável.

                ## Quando a informação NÃO está nos documentos (FALLBACK)
                Se a pergunta não puder ser respondida com os trechos disponíveis, siga estas regras:
                1. Diga claramente: "Não encontrei essa informação nos documentos disponíveis."
                2. Sugira o canal de contato mais adequado da lista abaixo:
                   - RH / People → #help-rh
                   - TI / Suporte Técnico → #help-ti
                   - Segurança da Informação → #security-reports
                   - Tech Lead da Squad → canal da squad (ex: #squad-agendio-core)
                   - Chapter (Back-end, Front-end, DevOps, Data) → canal do chapter (ex: #chapter-backend)
                   - Buddy → mensagem direta (DM)
                3. Para assuntos de Jurídico ou Financeiro (que não constam na base), diga explicitamente que o tema está fora do escopo da base atual e sugira procurar o RH (#help-rh) como ponto de partida.

                ## Formato da resposta
                Estruture sua resposta assim:
                1. **Resumo direto** — a resposta objetiva à pergunta, logo no início.
                2. **Referências** — ao final, cite os documentos e seções usados no formato:
                   📄 Fonte: [Nome do Documento], trecho relevante.
                   Se usou mais de um documento, liste cada fonte em linha separada.

                Esse formato é obrigatório para todas as respostas baseadas em documentos. Para saudações e respostas de fallback, não é necessário incluir referências.
                """;
    }

    /**
     * Monta o bloco de contexto com metadados de origem (nome do documento).
     * Cada trecho é rotulado para que o LLM possa citar a fonte na resposta.
     */
    private String montarContexto(List<TrechoComMetadado> trechos) {
        var sb = new StringBuilder();
        sb.append("Trechos relevantes dos documentos:\n\n");

        for (int i = 0; i < trechos.size(); i++) {
            var trecho = trechos.get(i);
            sb.append("--- Trecho ").append(i + 1).append(" ---\n");
            sb.append("[Fonte: ").append(trecho.documento()).append("]\n");
            sb.append(trecho.texto()).append("\n\n");
        }

        return sb.toString();
    }
}
