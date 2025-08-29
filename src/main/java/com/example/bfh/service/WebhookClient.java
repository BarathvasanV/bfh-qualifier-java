package com.example.bfh.service;

import com.example.bfh.dto.GenerateWebhookRequest;
import com.example.bfh.dto.GenerateWebhookResponse;
import com.example.bfh.dto.SubmitPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookClient {

    private final WebClient webClient = WebClient.builder().build();

    public GenerateWebhookResponse generate(String url, GenerateWebhookRequest req) {
        log.info("Calling generateWebhook at {}", url);
        return webClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(req))
                .retrieve()
                .bodyToMono(GenerateWebhookResponse.class)
                .doOnNext(r -> log.info("Received webhook URL and accessToken"))
                .block();
    }

    public void submitFinalQuery(String webhookUrl, String accessToken, String finalQuery) {
        log.info("Submitting finalQuery to webhook: {}", webhookUrl);
        webClient.post()
                .uri(webhookUrl)
                .header("Authorization", accessToken) // NOTE: spec says no Bearer prefix
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new SubmitPayload(finalQuery))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(body -> log.info("Submission response: {}", body))
                .onErrorResume(ex -> {
                    log.error("Submission failed: {}", ex.getMessage());
                    return Mono.empty();
                })
                .block();
    }
}
