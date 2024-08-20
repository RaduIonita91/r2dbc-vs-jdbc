package com.example.client_test.actuator;

import org.springframework.boot.actuate.metrics.MetricsEndpoint;

import java.util.List;

public record MetricsDTO(
    String name,
    String description,
    String baseUnit,
    List<Measurement> measurements,
    List<AvailableTag> availableTags) {}
