package com.tamara.batch.service;


import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class BatchService {

    private final WebClient webClient;

    public BatchService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:9000").build();
    }

    @Scheduled(fixedRate = 120000) // 2 minutos en milisegundos
    public void executeBatchProcess() {
        callOrchestrator();
    }

    private void callOrchestrator() {
        String jsonBody = """
        {
            "data": [
                {
                    "header": {
                        "id": "12345",
                        "type": "StepsGiraffeRefrigerator"
                    },
                    "enigma": "some_value"
                }
            ]
        }
        """;

        webClient.post()
                .uri("/orquestador/completeAnswer")  // Ajusta la URL según sea necesario
                .bodyValue(jsonBody)  // Envía el JSON en el cuerpo de la solicitud
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(response -> {
                    System.out.println("Orchestrator response: " + response);
                }, error -> {
                    System.err.println("Error calling orchestrator: " + error.getMessage());
                });
    }

}
