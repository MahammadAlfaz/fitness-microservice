package com.fitness.aiservice.service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service

public class GeminiService {
    private final WebClient webClient;

    public GeminiService(@Qualifier("plainWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    @Value("${gemini.api.url}")
    private String geminiApiUrl;

    @Value("${gemini.api.key}")
    private String geminiApiKey;



    public String getRecommendation(String details) {

        Map<String, Object> requestBody =
                Map.of(
                        "contents", List.of(
                                Map.of(
                                        "parts", List.of(
                                                Map.of("text", details)
                                        )
                                )
                        )
                );


            try {
                return webClient.post()
                        .uri(geminiApiUrl)
                        .header("Content-Type", "application/json")
                        .header("X-goog-api-key", geminiApiKey)
                        .bodyValue(requestBody)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (WebClientResponseException e) {
                log.error("Gemini API error: status={}, body={}",
                        e.getStatusCode(), e.getResponseBodyAsString());
                return null; // or fallback JSON
            } catch (Exception e) {
                log.error("Unexpected Gemini failure", e);
                return null;
            }
        }

    }
