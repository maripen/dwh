package com.adverity.dwh.remote.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class ReportRequest {

    @Schema(name = "calculatedField", description = "a calculated field, the base fields must have been be aggregated")
    private Map<String, String> calculatedField;

    @Schema(name = "collectionName", description = "collection name based on the imported csv filename")
    private String collectionName;

    @Schema(name = "filter", description = "an optional set of dimension filters to be filtered on")
    private Map<String, Map<FilterOperator, String>> filter;

    @Schema(name = "groupBy", description = "an optional set of dimensions to be grouped by")
    private Set<String> groupBy;

    @Schema(name = "aggregate", description = "a set of metrics to be aggregated on")
    private Map<String, AggregateOperators> aggregate;
}
