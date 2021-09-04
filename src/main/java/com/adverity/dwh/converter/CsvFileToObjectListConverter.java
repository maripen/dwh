package com.adverity.dwh.converter;


import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Component
public class CsvFileToObjectListConverter implements Converter<String, List<Map<String, Object>>> {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private final Map<Integer, Function<String, Optional<Object>>> parseLambdasForColumns = new HashMap<>();

    @Override
    @NonNull
    public List<Map<String, Object>> convert(String source) {
        var header = source.lines()
                .map(csvRow -> csvRow.split(","))
                .findFirst()
                .orElseThrow(); // TODO: create exception

        return source.lines()
                .skip(1)
                .map(csvRow -> csvRow.split(","))
                .map(csvRowFields -> parseRow(csvRowFields, header))
                .collect(Collectors.toList());
    }

    private Map<String, Object> parseRow(String[] csvRow, String[] header) {
        var result = new HashMap<String, Object>();
        for(int i = 0; i < csvRow.length; i++) {
            result.put(header[i], this.getFieldType(csvRow[i], i));
        }
        return result;
    }

    public Object getFieldType(String value, int index) { // TODO: move to util
        return parseLambdasForColumns
                .computeIfAbsent(index, s -> {
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
                })
                .apply(value)
                .orElse(value); // ?
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
}
