package com.adverity.dwh.remote;

import com.adverity.dwh.ImportService;
import com.adverity.dwh.remote.model.ImportRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import javax.validation.Valid;


@RestController
@RequestMapping(value = "/import")
public class ImportController {

    private ImportService importService;

    @Autowired
    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value="csv", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<Object>> importCsv(@Valid @RequestBody ImportRequest request) {
        final var fileUriPath = request.getUrl().getPath();
        if (fileUriPath.contains(".")) {
            final var extension = fileUriPath.substring(fileUriPath.lastIndexOf("."));
            if (extension.equalsIgnoreCase("csv")) {
                return this.importService
                        .importFromCsv(request.getUrl())
                        .map(unused -> ResponseEntity.ok().build());
            } else {
                return Mono.just(ResponseEntity.badRequest().body("File extension not supported for csv import"));
            }
        } else {
            return Mono.just(ResponseEntity.badRequest().body("Could not identify file extension"));
        }
    }
}
