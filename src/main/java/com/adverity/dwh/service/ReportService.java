package com.adverity.dwh.service;

import com.adverity.dwh.remote.model.FilterOperator;
import com.adverity.dwh.remote.model.ReportRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.model.Aggregates;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Projections.*;
import static com.mongodb.client.model.Projections.fields;

@Service
@Slf4j
public class ReportService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");

    @Autowired
    public ReportService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Map query(ReportRequest request) {
        logQuery(request);

        final Optional<MatchOperation> matchStage = buildMatchStage(request);
        final Optional<GroupOperation> groupByStage = buildGroupByStage(request);
        // final Optional<Bson> projectStage = getProjectionStage(request);

        return this.mongoTemplate.aggregate(Aggregation.newAggregation(matchStage.get(), groupByStage.get()),
                request.getCollectionName(),
                Map.class).getUniqueMappedResult();
        // return repository.aggregate(stages);
    }

    private Optional<MatchOperation> buildMatchStage(ReportRequest request) {
        final Map<String, Map<FilterOperator, String>> filter = request.getFilter();
        if (filter != null && !filter.isEmpty()) {
            final var filterCritria = filter.entrySet()
                    .stream()
                    .flatMap(
                            (fieldEntry -> {
                                final List<Criteria> criteriaList = new ArrayList<>();
                                fieldEntry.getValue().forEach((filterOperator, value) -> {
                                    try {
                                        Method filterMethod = Criteria.class.getMethod(filterOperator.name(), Object.class);
                                        criteriaList.add((Criteria) filterMethod.invoke(Criteria.where(fieldEntry.getKey()),
                                                this.getFieldType(value).apply(value)));
                                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                                        throw new IllegalArgumentException("Illegal filter operator");
                                    }
                                });
                                return criteriaList.stream();
                            })
                    )
                    .collect(Collectors.toList());

            return Optional.of(Aggregation.match(new Criteria().andOperator(filterCritria)));
        } else {
            return Optional.empty();
        }
    }

    public Function<String, Object> getFieldType(String value) { // TODO: move to util
        final Optional<Long> number = getNumber(value);
        if (number.isPresent()) {
            return val -> getNumber(val).map(aLong -> aLong);
        } else {
            final Optional<LocalDate> date = getDate(value);
            if (date.isPresent()) {
                return val -> getDate(val).map(localDate -> localDate);
            } else {
                return Optional::of;
            }
        }
    }

    private Optional<LocalDate> getDate(String value) {
        try {
            return Optional.of(LocalDate.from(DATE_FORMAT.parse(value)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<Long> getNumber(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<GroupOperation> buildGroupByStage(ReportRequest request) {
        if (request.getGroupBy() == null && request.getAggregate() == null) {
            return Optional.empty();
        } else {
            var groupOperation =
                    Aggregation.group(request.getGroupBy().toArray(new String[request.getGroupBy().size()]));
            // Method filterMethod = Criteria.class.getMethod(filterOperator.name(), Object.class);
            for (var aggregationEntry : request.getAggregate().entrySet()) {
                try {
                    Method aggregationMethod =
                            GroupOperation.class.getMethod(aggregationEntry.getValue().name(), String.class);
                    groupOperation = ((GroupOperation.GroupOperationBuilder) aggregationMethod
                            .invoke(groupOperation, aggregationEntry.getKey())).as(aggregationEntry.getKey());
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    throw new IllegalArgumentException("Illegal group by or aggregate operator");
                }
            }
            return Optional.of(groupOperation);
        }
    }

    private Optional<Bson> getProjectionStage(ReportRequest request) {
        List<Bson> projectionIncludeGroupId = new ArrayList<>();
        if (request.getGroupBy() != null) {
            request.getGroupBy().forEach(groupBy -> {
                projectionIncludeGroupId.add(computed(groupBy, "$_id." + groupBy));
            });
        }

        final ArrayList<Bson> projectionFields = new ArrayList<>();
        if (request.getAggregate() != null) {
            request.getAggregate().keySet().forEach(s -> projectionFields.add(include(s)));
        }
        if (!projectionIncludeGroupId.isEmpty()) {
            projectionFields.add(excludeId());
            projectionFields.addAll(projectionIncludeGroupId);
            return Optional.of(Aggregates.project(fields(projectionFields)));
        } else {
            return Optional.empty();
        }
    }

    private void logQuery(Object request) {
        if (log.isDebugEnabled()) {
            try {
                log.debug("Running query : " + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(request));
            } catch (JsonProcessingException e) {
                log.error("Failed to log query", e);
            }
        }
    }

}
