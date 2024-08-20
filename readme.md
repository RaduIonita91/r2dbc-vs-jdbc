# R2DBC vs JDBC

## How to use
1. Run _docker-compose.yml_ file to start the Postgres DB;
2. Start JdbcCrudApplication application (this will create the DB table if not already present);
3. Start ReactiveCrudApplication application;
4. Execute tests: ```r2dbc-vs-jdbc/client-test/src/test/java/com/example/client_test/client/CombinedTest.java```


## Project overview

In this project we are doing a comparison from speed and resources consumption perspective between classic approach,
synchronized, using JDBC driver and async approach, using R2DBC library.<br>

High level comparison: https://www.baeldung.com/jdbc-vs-r2dbc-vs-spring-jdbc-vs-spring-data-jdbc

For achieving this, we have three Spring Boot applications:

- **reactive-crud**: exposes REST endpoints for CRUD operations using R2DBC standard.
- **jdbc-crud**: exposes REST endpoints for CRUD operations using JDBC standard.
- **client-test**: consumes the endpoints exposed by the other 2 applications and copares the results in terms of
  response time, memory usage, open threads, http requests and other metrics.

The model:

```
TABLE client (
    id BIGINT,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255),
    rank VARCHAR(255),
    details JSONB
    )
```

The reason of adding a special Jsonb filed is for testing the conversion in both libraries.

For achieving all of these, a Postgres DB was created.
Details in _docker-compose.yml_ file.

## JDBC usage details

The classical implementation details were followed:

- pom dependencies;
- ClientEntity;
- ClientRepository;
- ClientService;
- ClientController;

On top of these, flyway was added for managing scripts.
For the moment, there's only 1 script with the Client table creation.

## R2DBC usage details

The usage of the R2DBC library is similar with JDBC. <br>
A Spring Boot starter package is available:

```
spring-boot-starter-data-r2dbc
```

Also, this library can be used in pair with Webflux, a framework used for handling HTTP requests reactively:

```
spring-boot-starter-webflux
```

### Repositories

All the CRUD operations are available by extending the ReactiveCrudRepository.
It is also possible to have custom queries by passing them as native queries in the @Query annotation:

```
@Query("select count (*) from client c where c.rank = :rank")
Mono<Long> countByRank(String rank);
```

### Entities

The entity declaration uses the standard Spring annotations: _@Entity, @Id, @Column_, etc. <br>
Compared to the JPA approach, here are much less available annotations for complex operations. For example, for
converting a filed, in JPA we can use _@ColumnTransformer_:

```
@ColumnTransformer(write = "?::jsonb")
private String details;
```

In R2DBC case, we need to create explicitly all the converters and add them in the _R2dbcCustomConversions_ bean.

```
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

  @Bean
  public R2dbcCustomConversions r2dbcCustomConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(new JsonNodeToJsonConverter());
    converters.add(new JsonToJsonNodeConverter());
    return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
  }
```

### Exception Handling
Exception handling is easy given the possibility to chain multiple methods.
A few examples:
- onErrorResume;
- onErrorComplete;
- switchIfEmpty;
- onErrorMap;

The methods accept different input types, making the usage easy, being able to treat different exception types, defining custom exceptions, or ignoring the expected ones.<br>
Please have a look in the _reactive-crud_ -> ClientService.java for some implementation examples


## Test Results

### Actuators
For getting a deeper understanding of resource consumption, both applications have the actuators endpoints activate.<br>
A small service, ActuatorService, is present in _client-test_ application that is able to get the desired metrics from the other 2 app servers.

### Scenarios
Most relevant test scenarios:
- parallel/concurrent inserts for different values: 1, 10, 100, 200;
- serial inserts for different values: 1, 10, 100, 200;
- getting all the data;

### Results

Single insert:
```
Synchronous: insert method duration: 64 ms
Reactive: insert method duration: 1 ms
```

Serial inserts:
```
===== Setup =====
Number of inserts: 200
Delay between requests: 2000ms
===== Results =====
Synchronous: total serial insert method duration: 431702 ms
Reactive: total serial insert method duration: 426253 ms
Synchronous: average serial insert method duration: 2158 ms
Reactive: average serial insert method duration: 2131 ms
```

Parallel inserts:
```
===== Setup =====
Number of inserts: 200
===== Results =====
Synchronous: parallel inserts method duration: 1544 ms
Synchronous: Response size: 200 rows
Reactive: parallel inserts method duration: 1795 ms
Reactive: Response size: 200 rows
Reactive: total serial insert method duration: 1795 ms
Synchronous: average serial insert method duration: 7 ms
Reactive: average serial insert method duration: 8 ms
============
Reactive: MetricsHttpServerRequests: measurements=[Measurement[statistic=COUNT, value=468.0], Measurement[statistic=TOTAL_TIME, value=82.25677], Measurement[statistic=MAX, value=0.49893716]]]
Synchronous: MetricsHttpServerRequests: measurements=[Measurement[statistic=COUNT, value=1393.0], Measurement[statistic=TOTAL_TIME, value=180.0366], Measurement[statistic=MAX, value=1.182852]]]
react_jvm_threads_started: MetricsDTO[name=jvm.threads.started, description=The total number of application threads started in the JVM, baseUnit=threads, measurements=[Measurement[statistic=COUNT, value=36.0]], availableTags=[]]
sync_jvm_threads_started: MetricsDTO[name=jvm.threads.started, description=The total number of application threads started in the JVM, baseUnit=threads, measurements=[Measurement[statistic=COUNT, value=220.0]], availableTags=[]]
react_process_cpu_usage: MetricsDTO[name=process.cpu.usage, description=The "recent cpu usage" for the Java Virtual Machine process, baseUnit=null, measurements=[Measurement[statistic=VALUE, value=0.11917462]], availableTags=[]]
sync_process_cpu_usage: MetricsDTO[name=process.cpu.usage, description=The "recent cpu usage" for the Java Virtual Machine process, baseUnit=null, measurements=[Measurement[statistic=VALUE, value=0.10724431]], availableTags=[]]
```

Get all clients:
```
Synchronous: select all method duration: 470 ms
Synchronous: Response size: 3744 rows
Reactive: select all method duration: 5 ms
Reactive: Response size: 3744 rows
```

## Conclusions

As noticed from the test results, reactive/async approach is significantly faster and much more efficient with big number of serial inserts. <br>
For parallel inserts, there are not so many differences in terms of time, but from resource usage it is still better. <br>
A special case is the _getAll_ test because the sync endpoint returns a List<Client> while the async endpoint returns a Flux<Client>. If the Flux was blocked and a List would have been returned, the time would be similar with the sync one. <br>
Reasons to keep it as Flux:
- Maintain non-blocking/reactive behaviour;
- Flux supports backpressure, meaning it can handle large data streams efficiently by sending it in chunks.
- The client can start processing the incoming data as soon as the first element is available.
