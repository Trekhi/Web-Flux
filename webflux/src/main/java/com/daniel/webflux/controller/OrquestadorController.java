package com.daniel.webflux.controller;

import com.daniel.webflux.service.AnswerCompleted;
import com.daniel.webflux.service.StepOneService;
import com.daniel.webflux.service.StepThreeService;
import com.daniel.webflux.service.StepTwoService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/orquestador")
public class OrquestadorController {

    private final StepOneService stepOneService;
    private final StepTwoService stepTwoService;
    private final StepThreeService stepThreeService;
    private final AnswerCompleted answerCompleted;



    // Inyectar los servicios en el constructor
    public OrquestadorController(StepOneService stepOneService, StepTwoService stepTwoService, StepThreeService stepThreeService, AnswerCompleted answerCompleted) {
        this.stepOneService = stepOneService;
        this.stepTwoService = stepTwoService;
        this.stepThreeService = stepThreeService;
        this.answerCompleted = answerCompleted;
    }

    // Llamar al StepOneService desde un endpoint
    @PostMapping("/stepOne")
    public Mono<ResponseEntity<String>> callStepOne(@RequestBody String requestBody) {
        return stepOneService.callStepOne(requestBody)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error en Step 1: " + throwable.getMessage())));
    }

    // Llamar al StepTwoService desde otro endpoint
    @PostMapping("/stepTwo")
    public Mono<ResponseEntity<String>> callStepTwo(@RequestBody String requestBody) {
        return stepTwoService.StepTwoCall(requestBody)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error en Step 2: " + throwable.getMessage())));
    }

    // Llamar al StepThreeService desde otro endpoint
    @PostMapping("/stepThree")
    public Mono<ResponseEntity<String>> callStepThree(@RequestBody String requestBody) {
        return stepThreeService.StepThreeCall(requestBody)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error en Step 3: " + throwable.getMessage())));
    }

    // Llamar al AnswerCompleted para unificar todas las respuestas
    @PostMapping("/completeAnswer")
    public Mono<ResponseEntity<String>> getCompleteAnswer(@RequestBody String requestBody) {
        return answerCompleted.callAllSteps(requestBody)
                .map(response -> ResponseEntity.ok(response))
                .onErrorResume(throwable -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Error en la respuesta completa: " + throwable.getMessage())));
    }





}
