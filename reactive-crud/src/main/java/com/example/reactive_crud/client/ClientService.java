package com.example.reactive_crud.client;

import com.example.reactive_crud.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ClientService {

  @Autowired ClientRepository clientRepository;

  private static final ObjectMapper objectMapper = new ObjectMapper();

  public Flux<ClientDTO> getAll() {
    Flux<Client> all = clientRepository.findAll().onErrorResume(ex -> Mono.empty());
    //            .onErrorComplete();

    return all.map(ClientMapper::fromEntityToDTO);
  }

  public Mono<ClientDTO> getById(Long id) {
    Mono<Client> client =
        clientRepository
            .findById(id)
            //            .switchIfEmpty(Mono.empty())
            .switchIfEmpty(Mono.error(new RuntimeException("Client not found!")))
            .onErrorMap(
                DataAccessResourceFailureException.class,
                ex -> new CustomException("Access Error", ex));

    return client.map(ClientMapper::fromEntityToDTO);
  }

  public Mono<ClientDTO> save(Long id, ClientDTO dto) {
    if (id != null) {
      Mono<Client> saved =
          clientRepository
              .findById(id)
              .flatMap(
                  old -> {
                    old.setFirstName(dto.firstName());
                    old.setLastName(dto.lastName());
                    old.setEmail(dto.email());
                    old.setRank(dto.rank());
                    try {
                      old.setDetails(objectMapper.readTree(dto.details()));
                    } catch (JsonProcessingException e) {
                      e.printStackTrace();
                    }
                    return clientRepository
                        .save(old)
                        .onErrorMap(
                            DuplicateKeyException.class,
                            ex -> new CustomException("Duplicate key!"))
                        .onErrorMap(
                            DataIntegrityViolationException.class,
                            ex -> new CustomException("Data integrity violation!", ex));
                  });
      return saved.map(ClientMapper::fromEntityToDTO);
    } else {
      Client client = ClientMapper.fromDTOToEntity(dto);
      Mono<Client> saved =
          clientRepository
              .save(client)
              .onErrorMap(DuplicateKeyException.class, ex -> new CustomException("Duplicate key!"));
      return saved.map(ClientMapper::fromEntityToDTO);
    }
  }

  public Mono<Void> delete(Long id) {
    return clientRepository.deleteById(id);
  }

  public Mono<Long> countByRank(String rank) {
    return clientRepository.countByRank(rank);
  }
}
