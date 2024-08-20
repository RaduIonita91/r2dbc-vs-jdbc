package com.example.reactive_crud.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.postgresql.codec.Json;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class JsonToJsonNodeConverter implements Converter<Json, JsonNode> {
  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public JsonNode convert(Json source) {
    try {
      return objectMapper.readTree(source.asString());
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    return null;
  }
}
