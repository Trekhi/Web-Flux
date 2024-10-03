package com.daniel.webflux.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class AnswerCompleted {

    private final StepOneService stepOneService;
    private final StepTwoService stepTwoService;
    private final StepThreeService stepThreeService;
    private final WebHookService webHookService;
    private static final Logger LOG = LoggerFactory.getLogger(AnswerCompleted.class);


    public AnswerCompleted(StepOneService stepOneService, StepTwoService stepTwoService, StepThreeService stepThreeService, WebHookService webHookService) {
        this.stepOneService = stepOneService;
        this.stepTwoService = stepTwoService;
        this.stepThreeService = stepThreeService;
        this.webHookService = webHookService;

    }

    public Mono<String> callAllSteps(String requestBody) {
        // Primero llama al WebHookStart y luego procesa los pasos
        return webHookService.WebHookStart()
                .then(
                        // Ejecutar los tres pasos después del WebHookStart
                        Mono.zip(
                                stepOneService.callStepOne(requestBody).map(this::extraerAnswer),
                                stepTwoService.StepTwoCall(requestBody).map(this::extraerAnswer),
                                stepThreeService.StepThreeCall(requestBody).map(this::extraerAnswer)
                        ).flatMap(tuple3 -> {
                            String answerStepOne = tuple3.getT1();
                            String answerStepTwo = tuple3.getT2();
                            String answerStepThree = tuple3.getT3();

                            // Crear la respuesta final
                            String finalAnswer = String.format(
                                    "{\"data\": [{\"header\": {\"id\": \"12345\", \"type\": \"TestGiraffeRefrigerator\"}, \"answer\": \"Step1: %s - Step2: %s - Step3: %s\"}]}",
                                    answerStepOne, answerStepTwo, answerStepThree
                            );

                            // Enviar la respuesta final al WebHook y retornar la respuesta final
                            return webHookService.WebHook(finalAnswer)
                                    .flatMap(webHookResponse ->
                                            Mono.just("Respuesta final enviada al WebHook: " + webHookResponse + "\nAnswered: " + finalAnswer)
                                    );
                        })
                )
                .onErrorResume(throwable -> {
                    // Manejo de errores
                    LOG.warn("Error en callAllSteps: " + throwable.getMessage());
                    return Mono.just("Error al procesar los pasos: " + throwable.getMessage());
                });
    }




    // Método para extraer el campo "answer" de la respuesta
    private String extraerAnswer(String response) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            return root.at("/0/data/0/answer").asText();  // Ruta hacia el campo "answer"
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "Error al procesar la respuesta";
        }
    }

}
