package com.example.client_test.client;

import java.util.List;
import java.util.Objects;
import java.util.Vector;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientR2DBCService {
  private final WebClient webClient;

  private final String baseUrl = "http://localhost:8080/r2dbc/client";

  public ClientR2DBCService(WebClient.Builder webClientBuilder) {
    this.webClient =
        webClientBuilder
            .codecs(config -> config.defaultCodecs().maxInMemorySize(1 * 1024 * 1024))
            .baseUrl(baseUrl)
            .build();
  }

  public Flux<ClientDTO> getAll() {
    return webClient.get().uri("").retrieve().bodyToFlux(ClientDTO.class);
  }

  public Mono<ClientDTO> getById(Long id) {
    return webClient.get().uri("/{id}", id).retrieve().bodyToMono(ClientDTO.class);
  }

  public Mono<ClientDTO> create(ClientDTO clientDTO) {
    return webClient
        .post()
        .uri("")
        .body(Mono.just(clientDTO), ClientDTO.class)
        .retrieve()
        .bodyToMono(ClientDTO.class);
  }

  public Mono<ClientDTO> update(Long id, ClientDTO clientDTO) {
    return webClient
        .put()
        .uri("/{id}", id)
        .body(Mono.just(clientDTO), ClientDTO.class)
        .retrieve()
        .bodyToMono(ClientDTO.class);
  }

  public Mono<Void> delete(Long id) {
    return webClient.delete().uri("/{id}", id).retrieve().bodyToMono(Void.class);
  }
}
