package com.sps_agente.demo;

import com.sps_agente.demo.rag.Chunker;
import com.sps_agente.demo.rag.ExtratorDePdf;
import com.sps_agente.demo.rag.IndiceVetorial;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Carrega os PDFs do diretório de documentos e indexa no startup.
 * Desabilitado em testes.
 */
@Component
@Profile("!test")
public class AgenteRunner implements CommandLineRunner {

    private final IndiceVetorial indice;
    private final String diretorioDocumentos;

    public AgenteRunner(IndiceVetorial indice,
                        @Value("${documentos.dir:documentos}") String diretorioDocumentos) {
        this.indice = indice;
        this.diretorioDocumentos = diretorioDocumentos;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Agente de IA sobre Documentos ===");
        System.out.println();

        carregarDocumentos();

        System.out.println("Backend pronto. Aguardando requisições HTTP...");
        System.out.println();
    }

    private void carregarDocumentos() {
        Path dir = Path.of(diretorioDocumentos);

        if (!Files.isDirectory(dir)) {
            System.err.println("AVISO: Diretório '" + diretorioDocumentos + "/' não encontrado.");
            System.err.println("O agente funcionará sem documentos até que o diretório seja criado.");
            return;
        }

        try {
            List<Path> pdfs = Files.list(dir)
                    .filter(p -> p.toString().toLowerCase().endsWith(".pdf"))
                    .toList();

            if (pdfs.isEmpty()) {
                System.err.println("AVISO: Nenhum PDF encontrado em '" + diretorioDocumentos + "/'.");
                return;
            }

            Chunker chunker = new Chunker();

            for (Path pdf : pdfs) {
                String nomeDocumento = extrairNomeDocumento(pdf);
                System.out.printf("Carregando: %s ... ", pdf.getFileName());
                try {
                    String texto = ExtratorDePdf.extrair(pdf);
                    List<String> trechos = chunker.fatiar(texto);
                    if (!trechos.isEmpty()) {
                        indice.indexar(trechos, nomeDocumento);
                    }
                    System.out.printf("%d trechos%n", trechos.size());
                } catch (IOException e) {
                    System.out.printf("ERRO: %s%n", e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("Erro ao listar diretório: " + e.getMessage());
        }

        System.out.println();
    }

    /**
     * Extrai o nome legível do documento a partir do nome do arquivo PDF.
     * Remove a extensão .pdf e limpa sufixos como "(PT-BR)".
     */
    private String extrairNomeDocumento(Path pdf) {
        String nome = pdf.getFileName().toString();
        if (nome.toLowerCase().endsWith(".pdf")) {
            nome = nome.substring(0, nome.length() - 4);
        }
        nome = nome.replaceAll("\\s*\\([^)]*\\)\\s*$", "").strip();
        nome = nome.replaceAll("\\s*[—-]\\s*$", "").strip();
        return nome;
    }
}
