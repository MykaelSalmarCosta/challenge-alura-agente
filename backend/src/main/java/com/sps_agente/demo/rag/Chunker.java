package com.sps_agente.demo.rag;

import java.util.ArrayList;
import java.util.List;


public class Chunker {

    private static final int TAMANHO_PADRAO = 1000;
    private static final int SOBREPOSICAO_PADRAO = 200;

    private final int tamanhoAlvo;
    private final int sobreposicao;

    public Chunker() {
        this(TAMANHO_PADRAO, SOBREPOSICAO_PADRAO);
    }

    public Chunker(int tamanhoAlvo, int sobreposicao) {
        this.tamanhoAlvo = tamanhoAlvo;
        this.sobreposicao = sobreposicao;
    }

    
    public List<String> fatiar(String texto) {
        if (texto == null || texto.isBlank()) return List.of();

        List<String> trechos = new ArrayList<>();
        int inicio = 0;

        while (inicio < texto.length()) {
            int fim = Math.min(inicio + tamanhoAlvo, texto.length());

            if (fim < texto.length()) {
                int quebraParagrafo = texto.lastIndexOf("\n\n", fim);
                if (quebraParagrafo > inicio) {
                    fim = quebraParagrafo;
                }
            }

            String trecho = texto.substring(inicio, fim).strip();
            if (!trecho.isEmpty()) {
                trechos.add(trecho);
            }

            int proximoInicio = fim - sobreposicao;
            if (proximoInicio <= inicio) {
                proximoInicio = fim;
            }
            inicio = proximoInicio;
        }

        return trechos;
    }
}
