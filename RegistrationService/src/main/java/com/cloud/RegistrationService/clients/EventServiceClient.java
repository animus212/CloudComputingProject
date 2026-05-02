package com.cloud.RegistrationService.clients;

import com.cloud.RegistrationService.dtos.response.EventSummaryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@Slf4j
public class EventServiceClient {
    private final WebClient webClient;

    public EventServiceClient(@Value("${services.event-service.base-url}") String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public EventSummaryDto reserveSpot(Long eventId) {
        try {
            return webClient.post()
                    .uri("/api/events/{id}/reserve", eventId)
                    .retrieve()
                    .bodyToMono(EventSummaryDto.class)
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to reserve spot for event {}: {} - {}",
                    eventId, e.getStatusCode(), e.getResponseBodyAsString());

            throw new RuntimeException("Cannot reserve spot: " + e.getMessage(), e);
        }
    }

    public void releaseSpot(Long eventId) {
        try {
            webClient.post()
                    .uri("/api/events/{id}/release", eventId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (WebClientResponseException e) {
            log.error("Failed to release spot for event {}: {}", eventId, e.getMessage());
        }
    }
}
