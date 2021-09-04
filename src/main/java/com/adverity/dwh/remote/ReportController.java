package com.adverity.dwh.remote;

import com.adverity.dwh.remote.model.ReportRequest;
import com.adverity.dwh.service.ReportService;
import com.mongodb.MongoCommandException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping(value = "/query", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(description = "Execute a query using request DSL")
    @RequestMapping(method = RequestMethod.POST)
    public Map<String, Object> query(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Query Json", required = true,
                    content = @Content(examples = @ExampleObject(value = "{ \"filter\": { \"Datasource\": { \"eq\": \"Google Ads\" } }, \"groupBy\": [ \"Campaign\" ], \"aggregate\": { \"Clicks\": \"sum\" } }")))
            @Valid @RequestBody ReportRequest request) {
        return reportService.query(request);
    }

    // ControllerAdvice
    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handle(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler
    public ResponseEntity<Map<String, String>> handle(MongoCommandException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

}
