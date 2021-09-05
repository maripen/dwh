package com.adverity.dwh.converter;


import com.adverity.dwh.util.TypeGuesser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

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
    private final TypeGuesser typeGuesser;

    @Autowired
    public CsvFileToObjectListConverter(TypeGuesser typeGuesser) {
        this.typeGuesser = typeGuesser;
    }

    @Override
    @NonNull
    public List<Map<String, Object>> convert(String source) {
        var header = source.lines()
                .map(csvRow -> csvRow.split(","))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Missing CSV header"));

        return source.lines()
                .skip(1)
                .map(csvRow -> csvRow.split(","))
                .map(csvRowFields -> parseRow(csvRowFields, header))
                .collect(Collectors.toList());
    }

    private Map<String, Object> parseRow(String[] csvRow, String[] header) {
        var result = new HashMap<String, Object>();
        for(int i = 0; i < csvRow.length; i++) {
            result.put(header[i], this.getOrCacheFieldType(csvRow[i], i));
        }
        return result;
    }

    public Object getOrCacheFieldType(String value, int index) {
        return parseLambdasForColumns
                .computeIfAbsent(index, s -> this.typeGuesser.getFieldType(value))
                .apply(value)
                .orElseThrow(() -> new IllegalArgumentException("Not consistent datatype in column: " + index));
    }
}
