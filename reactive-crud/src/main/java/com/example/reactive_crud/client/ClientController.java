package com.example.reactive_crud.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/r2dbc/client")
public class ClientController {

  @Autowired ClientService clientService;

  @GetMapping
  public Flux<ClientDTO> getAll() {
    return clientService.getAll();
  }

  @GetMapping("/{id}")
  public Mono<ClientDTO> getById(@PathVariable Long id) {
    return clientService.getById(id);
  }

  @PostMapping
  public Mono<ClientDTO> create(@RequestBody ClientDTO dto) {
    return clientService.save(null, dto);
  }

  @PutMapping("/{id}")
  public Mono<ClientDTO> update(@PathVariable Long id, @RequestBody ClientDTO dto) {
    return clientService.save(id, dto);
  }

  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable Long id) {
    return clientService.delete(id);
  }

  @GetMapping("/count")
  public Mono<Long> countByRank(@RequestParam String rank) {
    return clientService.countByRank(rank);
  }
}
