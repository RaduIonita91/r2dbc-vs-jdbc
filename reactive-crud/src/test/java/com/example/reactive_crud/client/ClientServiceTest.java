package com.example.reactive_crud.client;

import com.example.reactive_crud.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

  @Mock private ClientRepository clientRepository;

  @InjectMocks private ClientService clientService;

  @Test
  public void getAll_resumeOnError() {
    Mockito.doReturn(Flux.error(new DataIntegrityViolationException("")))
        .when(clientRepository)
        .findAll();

    StepVerifier.create(clientService.getAll()).expectComplete().verify();

    verify(clientRepository, times(1)).findAll();
  }

  @Test
  public void getById_empty() {
    Long id = 1L;
    Mockito.doReturn(Mono.empty()).when(clientRepository).findById(id);

    StepVerifier.create(clientService.getById(id))
        .expectErrorMatches(
            thr -> thr instanceof RuntimeException && thr.getMessage().equals("Client not found!"))
        .verify();

    verify(clientRepository, times(1)).findById(id);
  }

  @Test
  public void getById_accessError() {
    Long id = 1L;
    Mockito.doReturn(Mono.error(new DataAccessResourceFailureException("")))
        .when(clientRepository)
        .findById(id);

    StepVerifier.create(clientService.getById(id))
        .expectErrorMatches(
            thr -> thr instanceof CustomException && thr.getMessage().equals("Access Error"))
        .verify();

    verify(clientRepository, times(1)).findById(id);
  }

  @ParameterizedTest
  @MethodSource("provideExceptions")
  public void update_exceptions(RuntimeException ex, String message) {
    Long id = 1L;
    ClientDTO client = new ClientDTO(id, "Test", "test", "test@email.com", "Gold", "{}");

    Mockito.doReturn(Mono.error(ex)).when(clientRepository).save(any());
    Mockito.doReturn(Mono.fromSupplier(() -> ClientMapper.fromDTOToEntity(client)))
        .when(clientRepository)
        .findById(id);

    StepVerifier.create(clientService.save(1L, client))
        .expectErrorMatches(thr -> thr.getMessage().equals(message))
        .verify();

    verify(clientRepository, times(1)).save(any());
  }

  @Test
  public void save_exceptions() {
    Long id = 1L;
    ClientDTO client = new ClientDTO(id, "Test", "test", "test@email.com", "Gold", "{}");

    Mockito.doReturn(Mono.error(new DuplicateKeyException(""))).when(clientRepository).save(any());

    StepVerifier.create(clientService.save(null, client))
        .expectErrorMatches(thr -> thr.getMessage().equals("Duplicate key!"))
        .verify();

    verify(clientRepository, times(1)).save(any());
  }

  private static Stream<Arguments> provideExceptions() {
    return Stream.of(
        Arguments.of(new DuplicateKeyException(""), "Duplicate key!"),
        Arguments.of(new DataIntegrityViolationException(""), "Data integrity violation!"));
  }
}
