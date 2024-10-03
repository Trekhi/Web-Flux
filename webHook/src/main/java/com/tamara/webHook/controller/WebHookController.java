package com.tamara.webHook.controller;


import com.tamara.webHook.service.RequestService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

@RestController
public class WebHookController {

    private final RequestService requestService;

    public WebHookController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateWebHook(@RequestBody String jsonBody) {

        CompletableFuture<Void> result = requestService.processAsynchronously(jsonBody);
        return new ResponseEntity<>("Solicitud recibida y procesada de forma asincrónica.", HttpStatus.ACCEPTED);
    }

    // Endpoint GET que inicia el proceso del orquestador
    @GetMapping("/start")
    public Mono<String> startOrquestador() {
        return Mono.just("Orquestador iniciado con éxito.");
    }

}
