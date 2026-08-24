package com.sps_agente.demo.oci;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OciConfig {

    @Bean
    @ConditionalOnProperty("oci.objectstorage.bucket")
    public OciDocumentosProvider ociDocumentosProvider(
            @Value("${oci.objectstorage.region:sa-saopaulo-1}") String region,
            @Value("${oci.objectstorage.namespace}") String namespace,
            @Value("${oci.objectstorage.bucket}") String bucket,
            @Value("${oci.objectstorage.arquivos}") List<String> arquivos) {

        return new OciDocumentosProvider(region, namespace, bucket, arquivos);
    }
}
