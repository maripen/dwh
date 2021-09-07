package com.adverity.dwh.remote;

import com.adverity.dwh.remote.model.ImportRequest;
import com.adverity.dwh.service.ImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@RestController
@RequestMapping(value = "/import")
@Slf4j
public class ImportController {

    private ImportService importService;

    @Autowired
    public ImportController(ImportService importService) {
        this.importService = importService;
    }

    @PostMapping(value="csv", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importCsv(@Valid @RequestBody ImportRequest request) {
        var fileName = this.importService.importFile(request.getUrl());
        return ResponseEntity.ok("The CSV file has been imported as collectionName: " + fileName);
    }
}
