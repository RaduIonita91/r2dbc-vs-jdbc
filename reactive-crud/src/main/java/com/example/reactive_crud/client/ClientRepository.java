package com.example.reactive_crud.client;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ClientRepository extends ReactiveCrudRepository<Client, Long> {

    @Query("select count (*) from client c where c.rank = :rank")
    Mono<Long> countByRank(String rank);
}
