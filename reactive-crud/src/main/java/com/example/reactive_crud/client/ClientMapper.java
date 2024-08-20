package com.example.reactive_crud.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientMapper {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public static ClientDTO fromEntityToDTO(Client entity) {
    ClientDTO clientDTO = null;
    try {
      clientDTO =
          new ClientDTO(
              entity.getId(),
              entity.getFirstName(),
              entity.getLastName(),
              entity.getEmail(),
              entity.getRank(),
              objectMapper.writeValueAsString(entity.getDetails()));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return clientDTO;
  }

  public static Client fromDTOToEntity(ClientDTO dto) {
    Client client = new Client();
    client.setId(dto.id());
    client.setFirstName(dto.firstName());
    client.setLastName(dto.lastName());
    client.setEmail(dto.email());
    client.setRank(dto.rank());
    try {
      client.setDetails(objectMapper.readTree(dto.details()));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return client;
  }
}
