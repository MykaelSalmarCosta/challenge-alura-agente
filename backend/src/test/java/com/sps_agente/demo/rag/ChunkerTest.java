package com.sps_agente.demo.rag;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkerTest {

    @Test
    void textoVazioRetornaListaVazia() {
        var chunker = new Chunker();
        assertTrue(chunker.fatiar("").isEmpty());
        assertTrue(chunker.fatiar(null).isEmpty());
        assertTrue(chunker.fatiar("   ").isEmpty());
    }

    @Test
    void textoCurtoRetornaTrechoUnico() {
        var chunker = new Chunker(100, 20);
        List<String> trechos = chunker.fatiar("Um texto curto.");
        assertEquals(1, trechos.size());
        assertEquals("Um texto curto.", trechos.getFirst());
    }

    @Test
    void textoLongoProduzMultiplosTrechos() {
        var chunker = new Chunker(50, 10);
        String texto = "a".repeat(200);
        List<String> trechos = chunker.fatiar(texto);
        assertTrue(trechos.size() > 1, "Deveria produzir mais de um trecho");
    }

    @Test
    void buscaPorPalavraChaveRanqueiaCorretamente() {
        List<String> trechos = List.of(
                "O gato dormiu no sofá",
                "Java é uma linguagem de programação",
                "O gato preto do vizinho fugiu"
        );

        List<String> resultado = BuscadorPorPalavraChave.buscar("gato preto", trechos, 2);
        assertEquals(2, resultado.size());
        // "O gato preto do vizinho fugiu" tem 2 termos, deveria vir primeiro
        assertTrue(resultado.getFirst().contains("preto"));
    }

    @Test
    void cossenoSimilaridadeVetoresIguaisRetornaUm() {
        double[] a = {1.0, 2.0, 3.0};
        double sim = IndiceVetorial.cossenoSimilaridade(a, a);
        assertEquals(1.0, sim, 0.0001);
    }

    @Test
    void cossenoSimilaridadeVetoresOrtogonaisRetornaZero() {
        double[] a = {1.0, 0.0};
        double[] b = {0.0, 1.0};
        double sim = IndiceVetorial.cossenoSimilaridade(a, b);
        assertEquals(0.0, sim, 0.0001);
    }
}
