package com.adverity.dwh.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;

@Component
public class TypeGuesser {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yy");

    public Function<String, Optional<Object>> getFieldType(String value) {
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

}
