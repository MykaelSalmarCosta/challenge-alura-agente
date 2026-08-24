package com.sps_agente.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod & !test")
public class VerificacaoDaChave implements CommandLineRunner {
    private final String apiKey;


    public VerificacaoDaChave(
        @Value("${cohere.api.key}")
            String apiKey
    ) {
        this.apiKey = apiKey;
    }

    @Override
    public void run(String... arg) {
        System.out.println(apiKey.length());
        System.out.println(apiKey.substring(apiKey.length() -4));
    }
}
