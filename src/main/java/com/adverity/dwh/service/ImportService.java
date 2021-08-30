package com.adverity.dwh.service;

import com.adverity.dwh.model.AdStatistics;
import com.adverity.dwh.repository.AdStatisticsReactiveRepository;
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
    private final AdStatisticsReactiveRepository adStatisticsReactiveRepository;

    @Autowired
    public ImportService(WebClient.Builder webClientBuilder,
                         AdStatisticsReactiveRepository adStatisticsReactiveRepository) {
        this.webClient = webClientBuilder.build();
        this.adStatisticsReactiveRepository = adStatisticsReactiveRepository;
    }

    public Mono<Boolean> importFile(URI fileUri) {
        log.debug("Starting import of file {}", fileUri);
        final String filename = fileUri.getPath();

        var csvFile = webClient.get()
                .uri(fileUri)
                .retrieve()
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(5, Duration.ofMillis(500))
                        .doBeforeRetry(retrySignal -> {
                            log.info("Failed to retrieve file {} because of {} . Will retry", fileUri, retrySignal.failure().getMessage());
                        })
                );

        return saveCsvFile(filename, csvFile)
                .flatMap(savedEntity -> Mono.just(true))
                .reduce(Boolean.TRUE, (initial, result) -> initial && result)
                .doOnNext(result -> {
                    if (result) {
                        log.info("Successfully imported file {}", fileUri);
                    } else {
                        log.info("Failed to ingest file {}. See previous errors.", fileUri);
                    }
                });
    }

    private Flux<AdStatistics> saveCsvFile(String filename, Flux<String> csvFileStream) {
        final Mono<Void> deleteImported = adStatisticsReactiveRepository.deleteAll();

        return deleteImported.thenMany(csvFileStream)
                .skip(1) //skip the headers row
                .map(row -> row.split(","))
                .map(rowVal -> new AdStatistics(rowVal))
                .buffer(50)
                .flatMap(adStatisticsReactiveRepository::saveAll);
    }
}
