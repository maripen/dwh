package com.adverity.dwh.service;

import com.adverity.dwh.converter.CsvFileToObjectListConverter;
import com.adverity.dwh.repository.AdItemsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Objects;

@Service
@Slf4j
public class ImportService {

    private final RestTemplate restTemplate;
    private final AdItemsRepository adItemsRepository;

    private final CsvFileToObjectListConverter csvFileToObjectListConverter;

    @Autowired
    public ImportService(RestTemplateBuilder restTemplateBuilder,
                         AdItemsRepository adItemsRepository,
                         CsvFileToObjectListConverter csvFileToObjectListConverter) {
        this.restTemplate = restTemplateBuilder.build();
        this.adItemsRepository = adItemsRepository;
        this.csvFileToObjectListConverter = csvFileToObjectListConverter;
    }

    public void importFile(URI fileUri) {
        log.debug("Starting import of file {}", fileUri);
        final String filename = fileUri.getPath();
        var csvFile = restTemplate.getForObject(fileUri, String.class);
        Objects.requireNonNull(csvFile, "Invalid URL or file not available");
        this.adItemsRepository.deleteCollection(filename);
        this.adItemsRepository.saveAll(this.csvFileToObjectListConverter.convert(csvFile), filename);
    }
}
