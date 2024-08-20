package com.example.reactive_crud.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;

@Configuration
public class R2dbcConfiguration {

  @Bean
  public R2dbcCustomConversions r2dbcCustomConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(new JsonNodeToJsonConverter());
    converters.add(new JsonToJsonNodeConverter());
    return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
  }
}
