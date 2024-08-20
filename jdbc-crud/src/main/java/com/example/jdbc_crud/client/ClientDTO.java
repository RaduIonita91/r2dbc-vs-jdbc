package com.example.jdbc_crud.client;

public record ClientDTO(
        Long id,
        String firstName,
        String lastName,
        String email,
        String rank,
        String details
) {
}
