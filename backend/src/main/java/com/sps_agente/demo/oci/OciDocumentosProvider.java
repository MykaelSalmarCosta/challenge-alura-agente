package com.sps_agente.demo.oci;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


/**
 * Baixa PDFs de um bucket público do OCI Object Storage para um diretório local temporário.
 * Usa HTTP puro — sem SDK OCI.
 */
public class OciDocumentosProvider {

    private static final Logger log = LoggerFactory.getLogger(OciDocumentosProvider.class);

    private final String baseUrl;
    private final List<String> nomesDosArquivos;
    private final HttpClient httpClient;

    /**
     * @param region     região OCI (ex: sa-saopaulo-1)
     * @param namespace  namespace do tenancy
     * @param bucket     nome do bucket
     * @param nomesDosArquivos nomes dos PDFs no bucket
     */
    public OciDocumentosProvider(String region, String namespace, String bucket,
                                  List<String> nomesDosArquivos) {
        this.baseUrl = String.format(
                "https://objectstorage.%s.oraclecloud.com/n/%s/b/%s/o/",
                region, namespace, bucket);
        this.nomesDosArquivos = nomesDosArquivos;
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Baixa todos os PDFs configurados para o diretório de destino.
     * Retorna a lista de Paths dos arquivos baixados com sucesso.
     */
    public List<Path> baixarPara(Path diretorio) throws IOException {
        Files.createDirectories(diretorio);
        List<Path> baixados = new ArrayList<>();

        for (String nome : nomesDosArquivos) {
            String url = baseUrl + codificarNome(nome);
            Path destino = diretorio.resolve(nome);

            log.info("Baixando do OCI: {} ...", nome);

            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();

                HttpResponse<InputStream> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() == 200) {
                    try (InputStream body = response.body()) {
                        Files.copy(body, destino, StandardCopyOption.REPLACE_EXISTING);
                    }
                    baixados.add(destino);
                    log.info("  OK ({} bytes)", Files.size(destino));
                } else {
                    log.error("  Falha HTTP {} ao baixar {}", response.statusCode(), nome);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Download interrompido: " + nome, e);
            }
        }

        return baixados;
    }

    private String codificarNome(String nome) {
        return nome.replace(" ", "%20");
    }
}
