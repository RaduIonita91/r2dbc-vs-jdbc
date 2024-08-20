package com.example.jdbc_crud.client;

public class ClientMapper {

  public static ClientDTO fromEntityToDTO(Client entity) {
    return new ClientDTO(
        entity.getId(),
        entity.getFirstName(),
        entity.getLastName(),
        entity.getEmail(),
        entity.getRank(),
        entity.getDetails());
  }

  public static Client fromDTOToEntity(ClientDTO dto) {
    Client client = new Client();
    client.setId(dto.id());
    client.setFirstName(dto.firstName());
    client.setLastName(dto.lastName());
    client.setEmail(dto.email());
    client.setRank(dto.rank());
    client.setDetails(dto.details());
    return client;
  }
}
