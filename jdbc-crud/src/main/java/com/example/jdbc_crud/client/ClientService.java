package com.example.jdbc_crud.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

  @Autowired ClientRepository clientRepository;

  public List<ClientDTO> getAll() {
    List<Client> all = clientRepository.findAll();
    return all.stream().map(ClientMapper::fromEntityToDTO).toList();
  }

  public ClientDTO getById(Long id) {
    Client client =
        clientRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("no client found with id " + id));
    return ClientMapper.fromEntityToDTO(client);
  }

  public ClientDTO save(Long id, ClientDTO dto) {
    Client client = null;
    if (id != null) {
      client = clientRepository.findById(id).orElse(null);
    } else {
      client = new Client();
    }
    client.setFirstName(dto.firstName());
    client.setLastName(dto.lastName());
    client.setEmail(dto.email());
    client.setRank(dto.rank());
    client.setDetails(dto.details());
    Client saved = clientRepository.save(client);
    return ClientMapper.fromEntityToDTO(saved);
  }

  public void delete(Long id) {
    clientRepository.deleteById(id);
  }
}
