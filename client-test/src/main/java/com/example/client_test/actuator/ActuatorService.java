package com.example.client_test.actuator;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ActuatorService {
  private final RestTemplate restTemplate;

  private final String URI = "http://localhost:";
  private final String path = "/actuator/metrics/";

  public ActuatorService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public MetricsDTO getByMetrics(String port, String metric) {
    String url = URI + port + path + metric;
    return restTemplate.getForObject(url, MetricsDTO.class);
  }
}
