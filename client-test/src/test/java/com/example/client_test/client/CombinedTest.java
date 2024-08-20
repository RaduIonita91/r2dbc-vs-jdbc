package com.example.client_test.client;

import com.example.client_test.actuator.ActuatorService;
import com.example.client_test.actuator.MetricsDTO;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static com.example.client_test.actuator.MetricsConstants.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class CombinedTest {

  @Autowired ClientJDBCService clientJDBCService;
  @Autowired ClientR2DBCService clientR2DBCService;
  @Autowired ActuatorService actuatorService;

  @Test()
  @Order(5)
  public void getAllTest() {
    // Measure synchronous method time
    long syncStartTime = System.currentTimeMillis();
    List<ClientDTO> syncResult = clientJDBCService.getAll();
    long syncEndTime = System.currentTimeMillis();
    long syncDuration = syncEndTime - syncStartTime;

    // Measure reactive method time
    long reactiveStartTime = System.currentTimeMillis();
    Flux<ClientDTO> all = clientR2DBCService.getAll();
    long reactiveEndTime = System.currentTimeMillis();
    long reactiveDuration = reactiveEndTime - reactiveStartTime;
    //    List<ClientDTO> reactiveResult = all.collectList().block(Duration.ofSeconds(5));
    List<ClientDTO> reactiveResult = all.collectList().block();

    // Verify that both methods return the same number of results
    assertEquals(
        syncResult.size(),
        reactiveResult.size(),
        "Both methods should return the same number of results");

    // Print results
    System.out.println("Synchronous: select all method duration: " + syncDuration + " ms");
    System.out.println("Synchronous: Response size: " + syncResult.size() + " rows");
    System.out.println("Reactive: select all method duration: " + reactiveDuration + " ms");
    System.out.println("Reactive: Response size: " + reactiveResult.size() + " rows");
    generateMetricsReport();
  }

  @Test
  @Order(4)
  public void getByIdTest() {
    Long id = 2L;
    // Measure synchronous method time
    long syncStartTime = System.currentTimeMillis();
    ClientDTO syncResult = clientJDBCService.getById(id);
    long syncEndTime = System.currentTimeMillis();
    long syncDuration = syncEndTime - syncStartTime;

    // Measure reactive method time
    long reactiveStartTime = System.currentTimeMillis();
    Mono<ClientDTO> clientDTOMono = clientR2DBCService.getById(id);
    long reactiveEndTime = System.currentTimeMillis();
    long reactiveDuration = reactiveEndTime - reactiveStartTime;
    ClientDTO reactiveResult = clientDTOMono.block();

    // Check results not null
    assertNotNull(syncResult);
    assertNotNull(reactiveResult);

    // Print results
    System.out.println("Synchronous: get by id method duration: " + syncDuration + " ms");
    System.out.println("Reactive: get by id method duration: " + reactiveDuration + " ms");
    generateMetricsReport();
  }

  @Test
  @Order(1)
  public void insertTest_single() {
    ClientDTO clientDTO = getClientDTO();

    // Measure synchronous method time
    long syncStartTime = System.currentTimeMillis();
    ClientDTO syncResult = clientJDBCService.create(clientDTO);
    long syncEndTime = System.currentTimeMillis();
    long syncDuration = syncEndTime - syncStartTime;

    // Measure reactive method time
    long reactiveStartTime = System.currentTimeMillis();
    Mono<ClientDTO> reactiveResult = clientR2DBCService.create(clientDTO);
    long reactiveEndTime = System.currentTimeMillis();
    long reactiveDuration = reactiveEndTime - reactiveStartTime;

    // Print results

    System.out.println("Synchronous: insert method duration: " + syncDuration + " ms");
    System.out.println("Reactive: insert method duration: " + reactiveDuration + " ms");
    generateMetricsReport();
  }

  @ParameterizedTest
  @MethodSource("provideNumberOfInsertsAndDelays")
  @Order(2)
  public void insertTest_serial(Integer inserts, Integer delay) throws InterruptedException {
    ClientDTO clientDTO = getClientDTO();

    // Measure synchronous method time
    long syncStartTime = System.currentTimeMillis();
    for (int i = 0; i < inserts; i++) {
      ClientDTO syncResult = clientJDBCService.create(clientDTO);
      Thread.sleep(delay);
    }
    long syncEndTime = System.currentTimeMillis();
    long syncDuration = syncEndTime - syncStartTime;

    // Measure reactive method time
    long reactiveStartTime = System.currentTimeMillis();
    for (int i = 0; i < inserts; i++) {
      Mono<ClientDTO> reactiveResult = clientR2DBCService.create(clientDTO);
      Thread.sleep(delay);
    }
    long reactiveEndTime = System.currentTimeMillis();
    long reactiveDuration = reactiveEndTime - reactiveStartTime;

    // Print results
    System.out.println("===== Setup =====");
    System.out.println("Number of inserts: " + inserts);
    System.out.println("Delay between requests: " + delay + "ms");
    System.out.println("===== Results =====");
    System.out.println("Synchronous: total serial insert method duration: " + syncDuration + " ms");
    System.out.println(
        "Reactive: total serial insert method duration: " + reactiveDuration + " ms");
    System.out.println(
        "Synchronous: average serial insert method duration: " + syncDuration / inserts + " ms");
    System.out.println(
        "Reactive: average serial insert method duration: " + reactiveDuration / inserts + " ms");

    generateMetricsReport();
  }

  @ParameterizedTest
  @MethodSource("provideNumberOfInserts")
  @Order(3)
  public void insertTest_parallel(Integer inserts) throws InterruptedException {
    ClientDTO clientDTO = getClientDTO();

    ExecutorService executorServiceJDBC = Executors.newFixedThreadPool(inserts);
    List<Callable<ClientDTO>> tasksJDBC = new ArrayList<>();
    for (int i = 0; i < inserts; i++) {
      tasksJDBC.add(() -> clientJDBCService.create(clientDTO));
    }
    ExecutorService executorServiceR2DBC = Executors.newFixedThreadPool(inserts);
    List<Callable<ClientDTO>> tasksR2DBC = new ArrayList<>();
    for (int i = 0; i < inserts; i++) {
      tasksR2DBC.add(() -> clientR2DBCService.create(clientDTO).block());
    }

    // Measure synchronous method time
    long syncStartTime = System.currentTimeMillis();
    List<Future<ClientDTO>> futuresJDBC = executorServiceJDBC.invokeAll(tasksJDBC);
    long syncEndTime = System.currentTimeMillis();
    long syncDuration = syncEndTime - syncStartTime;

    // Measure reactive method time
    long reactiveStartTime = System.currentTimeMillis();
    List<Future<ClientDTO>> futuresR2DBC = executorServiceR2DBC.invokeAll(tasksR2DBC);
    long reactiveEndTime = System.currentTimeMillis();
    long reactiveDuration = reactiveEndTime - reactiveStartTime;

    executorServiceJDBC.shutdown();
    executorServiceR2DBC.shutdown();

    // Print results
    System.out.println("===== Setup =====");
    System.out.println("Number of inserts: " + inserts);
    System.out.println("===== Results =====");
    System.out.println("Synchronous: parallel inserts method duration: " + syncDuration + " ms");
    System.out.println("Synchronous: Response size: " + futuresJDBC.size() + " rows");
    System.out.println("Reactive: parallel inserts method duration: " + reactiveDuration + " ms");
    System.out.println("Reactive: Response size: " + futuresR2DBC.size() + " rows");
    System.out.println(
        "Reactive: total serial insert method duration: " + reactiveDuration + " ms");
    System.out.println(
        "Synchronous: average serial insert method duration: " + syncDuration / inserts + " ms");
    System.out.println(
        "Reactive: average serial insert method duration: " + reactiveDuration / inserts + " ms");

    generateMetricsReport();
  }

  private static Stream<Arguments> provideNumberOfInserts() {
    return Stream.of(Arguments.of(1), Arguments.of(10), Arguments.of(100), Arguments.of(200));
  }

  private static Stream<Arguments> provideNumberOfInsertsAndDelays() {
    return Stream.of(
        Arguments.of(1, 0),
        Arguments.of(10, 0),
        Arguments.of(100, 0),
        Arguments.of(200, 0),
        Arguments.of(100, 1000),
        Arguments.of(200, 1000),
        Arguments.of(100, 2000),
        Arguments.of(200, 2000));
  }

  private ClientDTO getClientDTO() {
    return new ClientDTO("firstName", "lastName", "email@test.com", "gold", "{\"\":\"lala\"}");
  }

  private void generateMetricsReport() {

    MetricsDTO reactiveMetricsHttpServerRequests =
        actuatorService.getByMetrics(R2DBC_PORT, HTTP_SERVER_REQUESTS);
    MetricsDTO synchronMetricsHttpServerRequests =
        actuatorService.getByMetrics(JDBC_PORT, HTTP_SERVER_REQUESTS);
    System.out.println("MetricsHttpServerRequests: " + synchronMetricsHttpServerRequests);
    System.out.println("MetricsHttpServerRequests: " + reactiveMetricsHttpServerRequests);

    MetricsDTO reavtive_executor_pool_max =
        actuatorService.getByMetrics(R2DBC_PORT, executor_pool_max);
    MetricsDTO sync_executor_pool_max = actuatorService.getByMetrics(JDBC_PORT, executor_pool_max);
    System.out.println("Reactive: reavtive_executor_pool_max: " + reavtive_executor_pool_max);
    System.out.println("sync_executor_pool_max: " + sync_executor_pool_max);

    MetricsDTO reactive_jvm_memory_used = actuatorService.getByMetrics(R2DBC_PORT, jvm_memory_used);
    MetricsDTO sync_jvm_memory_used = actuatorService.getByMetrics(JDBC_PORT, jvm_memory_used);
    System.out.println("reactive_jvm_memory_used: " + reactive_jvm_memory_used);
    System.out.println("sync_jvm_memory_used: " + sync_jvm_memory_used);

    MetricsDTO reactive_jvm_threads_peak =
        actuatorService.getByMetrics(R2DBC_PORT, jvm_threads_peak);
    MetricsDTO sync_jvm_threads_peak = actuatorService.getByMetrics(JDBC_PORT, jvm_threads_peak);
    System.out.println("reactive_jvm_threads_peak: " + reactive_jvm_threads_peak);
    System.out.println("sync_jvm_threads_peak: " + sync_jvm_threads_peak);

    MetricsDTO react_jvm_threads_started =
        actuatorService.getByMetrics(R2DBC_PORT, jvm_threads_started);
    MetricsDTO sync_jvm_threads_started =
        actuatorService.getByMetrics(JDBC_PORT, jvm_threads_started);
    System.out.println("react_jvm_threads_started: " + react_jvm_threads_started);
    System.out.println("sync_jvm_threads_started: " + sync_jvm_threads_started);

    MetricsDTO react_process_cpu_time = actuatorService.getByMetrics(R2DBC_PORT, process_cpu_time);
    MetricsDTO sync_process_cpu_time = actuatorService.getByMetrics(JDBC_PORT, process_cpu_time);
    System.out.println("react_process_cpu_time: " + react_process_cpu_time);
    System.out.println("sync_process_cpu_time: " + sync_process_cpu_time);

    MetricsDTO react_process_cpu_usage =
        actuatorService.getByMetrics(R2DBC_PORT, process_cpu_usage);
    MetricsDTO sync_process_cpu_usage = actuatorService.getByMetrics(JDBC_PORT, process_cpu_usage);
    System.out.println("react_process_cpu_usage: " + react_process_cpu_usage);
    System.out.println("sync_process_cpu_usage: " + sync_process_cpu_usage);

    MetricsDTO react_spring_data_repository_invocations =
        actuatorService.getByMetrics(R2DBC_PORT, spring_data_repository_invocations);
    MetricsDTO sync_spring_data_repository_invocations =
        actuatorService.getByMetrics(JDBC_PORT, spring_data_repository_invocations);
    System.out.println(
        "react_spring_data_repository_invocations: " + react_spring_data_repository_invocations);
    System.out.println(
        "sync_spring_data_repository_invocations: " + sync_spring_data_repository_invocations);

    MetricsDTO react_system_cpu_usage = actuatorService.getByMetrics(R2DBC_PORT, system_cpu_usage);
    MetricsDTO sync_system_cpu_usage = actuatorService.getByMetrics(JDBC_PORT, system_cpu_usage);
    System.out.println("react_system_cpu_usage: " + react_system_cpu_usage);
    System.out.println("sync_system_cpu_usage: " + sync_system_cpu_usage);

    MetricsDTO react_system_load_average_1m =
        actuatorService.getByMetrics(R2DBC_PORT, system_load_average_1m);
    MetricsDTO sync_system_load_average_1m =
        actuatorService.getByMetrics(JDBC_PORT, system_load_average_1m);
    System.out.println("react_system_load_average_1m: " + react_system_load_average_1m);
    System.out.println("sync_system_load_average_1m: " + sync_system_load_average_1m);

    MetricsDTO react_r2dbc_pool_acquired =
        actuatorService.getByMetrics(R2DBC_PORT, r2dbc_pool_acquired);
    MetricsDTO react_r2dbc_pool_max_allocated =
        actuatorService.getByMetrics(R2DBC_PORT, r2dbc_pool_max_allocated);
    System.out.println("react_r2dbc_pool_acquired: " + react_r2dbc_pool_acquired);
    System.out.println("react_r2dbc_pool_max_allocated: " + react_r2dbc_pool_max_allocated);

    MetricsDTO sync_hikaricp_connections_usage =
        actuatorService.getByMetrics(JDBC_PORT, hikaricp_connections_usage);
    MetricsDTO sync_hikaricp_connections_max =
        actuatorService.getByMetrics(JDBC_PORT, hikaricp_connections_max);
    MetricsDTO sync_hikaricp_connections =
        actuatorService.getByMetrics(JDBC_PORT, hikaricp_connections);
    System.out.println("sync_hikaricp_connections_usage: " + sync_hikaricp_connections_usage);
    System.out.println("sync_hikaricp_connections_max: " + sync_hikaricp_connections_max);
    System.out.println("sync_hikaricp_connections: " + sync_hikaricp_connections);
  }
}
