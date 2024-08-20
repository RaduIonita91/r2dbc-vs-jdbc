package com.example.client_test.actuator;

public class MetricsConstants {
  public static final String R2DBC_PORT = "8080";
  public static final String JDBC_PORT = "8081";

  public static final String HTTP_SERVER_REQUESTS = "http.server.requests";
  public static final String executor_pool_max = "executor.pool.max";
  public static final String jvm_memory_used = "jvm.memory.used";
  public static final String jvm_threads_peak = "jvm.threads.peak";
  public static final String jvm_threads_started = "jvm.threads.started";
  public static final String process_cpu_time = "process.cpu.time";
  public static final String process_cpu_usage = "process.cpu.usage";

  public static final String r2dbc_pool_acquired = "r2dbc.pool.acquired";
  public static final String r2dbc_pool_max_allocated = "r2dbc.pool.max.allocated";

  public static final String hikaricp_connections_max = "hikaricp.connections.max";
  public static final String hikaricp_connections_usage = "hikaricp.connections.usage";
  public static final String hikaricp_connections = "hikaricp.connections";

  public static final String spring_data_repository_invocations =
      "spring.data.repository.invocations";
  public static final String system_cpu_usage = "system.cpu.usage";
  public static final String system_load_average_1m = "system.load.average.1m";
}
