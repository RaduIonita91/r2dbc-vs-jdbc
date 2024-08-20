package com.example.reactive_crud.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("client")
public class Client {
  @Id private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String rank;
  private JsonNode details;

  public Client() {}

  public Client(String firstName, String lastName, String email, String rank, JsonNode details) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.email = email;
    this.rank = rank;
    this.details = details;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRank() {
    return rank;
  }

  public void setRank(String rank) {
    this.rank = rank;
  }

  public JsonNode getDetails() {
    return details;
  }

  public void setDetails(JsonNode details) {
    this.details = details;
  }
}
