package com.daniel.webflux.service;


import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
public class StepOneService {

    //Injectar variables en la clase para poder usar sus metodos dentro de la clase.
    private final WebClient webClient;
    private final Retry retry;
    private final CircuitBreaker circuitBreaker;
    private static final Logger LOG = LoggerFactory.getLogger(StepOneService.class);

    //Constructor para la configuración y especificiación de metodos Retry y Breaker
    public StepOneService(WebClient.Builder webClient, CircuitBreakerRegistry circuitBreakerRegistry, RetryRegistry retryRegistry) {
        this.webClient = webClient.build();
        this.retry = retryRegistry.retry("stepOne");
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("stepOne");

        this.retry.getEventPublisher()
                .onRetry(e -> LOG.info("Paso 1. \n Número de intentos: {}", e.getNumberOfRetryAttempts()));

        this.circuitBreaker.getEventPublisher()
                .onStateTransition(event -> LOG.info("Transición del Circuit Breaker para el paso 1: de {} a {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
    }

    public Mono<String> callStepOne(String requestBody) {
        return webClient.post()
                .uri("http://localhost:8080/getStep")  // URL del otro servicio
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)  // Pasar el JSON como String
                .retrieve()
                .bodyToMono(String.class)  // Esperar la respuesta como String
                .transformDeferred(RetryOperator.of(retry))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    LOG.warn("Demasiados reintentos para el paso 1, error: {}", throwable.getMessage());
                    return Mono.just("Step 1 no encontrado");
                })
                .doOnNext(body -> System.out.println("Respuesta del servicio: " + body));
    }
}
