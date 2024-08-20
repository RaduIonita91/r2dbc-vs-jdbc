package com.example.reactive_crud.client;

import com.fasterxml.jackson.databind.JsonNode;

public record ClientDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String rank,
        String details
) {
}
