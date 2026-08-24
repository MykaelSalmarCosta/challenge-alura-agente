package com.sps_agente.demo;

import com.sps_agente.demo.oci.OciDocumentosProvider;
import com.sps_agente.demo.rag.Chunker;
import com.sps_agente.demo.rag.ExtratorDePdf;
import com.sps_agente.demo.rag.IndiceVetorial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


/**
 * Carrega os PDFs e indexa no startup.
 * Usa OCI Object Storage quando configurado, senão lê do filesystem local.
 * Desabilitado em testes.
 */
@Component
@Profile("!test")
public class AgenteRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AgenteRunner.class);

    private final IndiceVetorial indice;
    private final String diretorioDocumentos;
    private final OciDocumentosProvider ociProvider;

    public AgenteRunner(IndiceVetorial indice,
                        @Value("${documentos.dir:documentos}") String diretorioDocumentos,
                        @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
                        org.springframework.beans.factory.ObjectProvider<OciDocumentosProvider> ociProviderRef) {
        this.indice = indice;
        this.diretorioDocumentos = diretorioDocumentos;
        this.ociProvider = ociProviderRef.getIfAvailable();
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== Agente de IA sobre Documentos ===");
        System.out.println();

        if (ociProvider != null) {
            carregarDocumentosOci();
        } else {
            carregarDocumentosLocal();
        }

        System.out.println("Backend pronto. Aguardando requisições HTTP...");
        System.out.println();
    }

    private void carregarDocumentosOci() {
        log.info("Modo OCI Object Storage ativado — baixando documentos do bucket...");

        Path dirTemp = Path.of(diretorioDocumentos);
        try {
            List<Path> pdfs = ociProvider.baixarPara(dirTemp);

            if (pdfs.isEmpty()) {
                log.warn("Nenhum PDF baixado do OCI.");
                return;
            }

            indexarPdfs(pdfs);

        } catch (IOException e) {
            log.error("Erro ao baixar documentos do OCI: {}", e.getMessage());
        }
    }

    private void carregarDocumentosLocal() {
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

            indexarPdfs(pdfs);

        } catch (IOException e) {
            System.err.println("Erro ao listar diretório: " + e.getMessage());
        }
    }

    private void indexarPdfs(List<Path> pdfs) {
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
