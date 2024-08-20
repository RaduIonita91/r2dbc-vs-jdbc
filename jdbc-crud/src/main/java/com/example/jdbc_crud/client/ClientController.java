package com.example.jdbc_crud.client;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/jdbc/client")
public class ClientController {

  @Autowired ClientService clientService;

  @GetMapping
  public List<ClientDTO> getAll() {
    return clientService.getAll();
  }

  @GetMapping("/{id}")
  public ClientDTO getById(@PathVariable Long id) {
    return clientService.getById(id);
  }

  @PostMapping
  public ClientDTO create(@RequestBody ClientDTO dto) {
    return clientService.save(null, dto);
  }

  @PutMapping("/{id}")
  public ClientDTO update(@PathVariable Long id, @RequestBody ClientDTO dto) {
    return clientService.save(id, dto);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    clientService.delete(id);
  }
}
