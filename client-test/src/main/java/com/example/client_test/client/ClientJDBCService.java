package com.example.client_test.client;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;

@Service
public class ClientJDBCService {
  private final RestTemplate restTemplate;

  private final String baseUrl = "http://localhost:8081/jdbc/client";

  public ClientJDBCService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public List<ClientDTO> getAll() {
    ResponseEntity<ClientDTO[]> clientsResponse =
        restTemplate.getForEntity(baseUrl, ClientDTO[].class);
    return List.of(Objects.requireNonNull(clientsResponse.getBody()));
  }

  public ClientDTO getById(Long id) {
    return restTemplate.getForObject(baseUrl + "/" + id, ClientDTO.class);
  }

  public ClientDTO create(ClientDTO clientDTO) {
    return restTemplate.postForObject(baseUrl, clientDTO, ClientDTO.class);
  }

  public ClientDTO update(Long id, ClientDTO clientDTO) {
    HttpEntity<ClientDTO> request = new HttpEntity<>(clientDTO);
    ResponseEntity<ClientDTO> responseEntity =
        restTemplate.exchange(baseUrl + "/" + id, HttpMethod.PUT, request, ClientDTO.class);
    return Objects.requireNonNull(responseEntity.getBody());
  }

  public void delete(Long id) {
    restTemplate.delete(baseUrl + "/" + id);
  }
}
