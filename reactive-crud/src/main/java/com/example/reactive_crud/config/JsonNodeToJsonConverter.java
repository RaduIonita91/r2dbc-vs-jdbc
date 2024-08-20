package com.example.reactive_crud.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class JsonNodeToJsonConverter implements Converter<JsonNode, Json> {
  private static final ObjectMapper objectMapper = new ObjectMapper();
  
  @Override
  public Json convert(JsonNode source) {
    try {
      return Json.of(objectMapper.writeValueAsString(source));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }
}
