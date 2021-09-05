package com.adverity.dwh.service;

import com.adverity.dwh.remote.model.FilterOperator;
import com.adverity.dwh.remote.model.ReportRequest;
import com.adverity.dwh.util.TypeGuesser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportService {

    private final MongoTemplate mongoTemplate;
    private final TypeGuesser typeGuesser;

    @Autowired
    public ReportService(MongoTemplate mongoTemplate,
                         TypeGuesser typeGuesser) {
        this.mongoTemplate = mongoTemplate;
        this.typeGuesser = typeGuesser;
    }

    public List<Map> query(ReportRequest request) {
        final var stages = new ArrayList<AggregationOperation>();
        buildMatchStage(request).ifPresent(stages::add);
        buildGroupByStage(request).ifPresent(stages::add);
        getProjectionStage(request).ifPresent(stages::add);

        return this.mongoTemplate.aggregate(Aggregation.newAggregation(
                stages),
                request.getCollectionName(),
                Map.class).getMappedResults();
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
                                                this.typeGuesser.getFieldType(value).apply(value).get()));
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

    private Optional<GroupOperation> buildGroupByStage(ReportRequest request) {
        if (request.getGroupBy() == null && request.getAggregate() == null) {
            return Optional.empty();
        } else {
            var groupOperation =
                    Aggregation.group(request.getGroupBy().toArray(new String[request.getGroupBy().size()]));
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

    private Optional<ProjectionOperation> getProjectionStage(ReportRequest request) {
        List<String> projectionFields = new ArrayList<>();

        if (request.getGroupBy() != null) {
            request.getGroupBy()
                    .stream()
                    .map(groupBy -> "$_id." + groupBy)
                    .collect(Collectors.toCollection(() -> projectionFields));
        }

        if (request.getAggregate() != null) {
            projectionFields.addAll(request.getAggregate().keySet());
        }

        if(projectionFields.isEmpty()) {
            return Optional.empty();
        }

        var projectionOperation = Aggregation.project(projectionFields.toArray(String[]::new));

        if(request.getCalculatedField() != null) {
            for(var calculatedField : request.getCalculatedField().entrySet()) {
                projectionOperation =
                        projectionOperation.andExpression(calculatedField.getValue()).as(calculatedField.getKey());
            }
        }

        return Optional.of(projectionOperation);

    }
}
