package com.cloud.NotificationService.clients;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Component
@Slf4j
public class RegistrationServiceClient {
    private final WebClient webClient;

    public RegistrationServiceClient(@Value("${services.registration-service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public List<Long> getAllEventUsers(Long eventId) {
        try {
            return webClient.get()
                    .uri("/api/registrations/event/internal/{eventId}", eventId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Long>>() {})
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to get ids for event {}: {} - {}",
                    eventId, e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Cannot get ids: " + e.getMessage(), e);
        }
    }
}
