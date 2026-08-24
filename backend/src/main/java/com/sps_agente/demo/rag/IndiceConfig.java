package com.sps_agente.demo.rag;

import com.sps_agente.demo.cohere.CohereClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndiceConfig {

    @Bean
    public IndiceVetorial indiceVetorial(CohereClient cohere) {
        return new IndiceVetorial(cohere);
    }
}
