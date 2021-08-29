package com.adverity.dwh;

import com.adverity.dwh.repository.DataRepository;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;

@Service
@Slf4j
public class ImportService {

    private final WebClient webClient;
    private final DataRepository dataRepository;

    @Autowired
    public ImportService(WebClient.Builder webClientBuilder, DataRepository dataRepository) {
        this.webClient = webClientBuilder.build();
        this.dataRepository = dataRepository;
    }



}
