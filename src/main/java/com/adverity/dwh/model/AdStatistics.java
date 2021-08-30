package com.adverity.dwh.model;

import lombok.Value;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document
@Value
public class AdStatistics {

    String dataSource;
    String campaign;
    Date   daily;
    Long   clicks;
    Long   impressions;

    public AdStatistics(String[] csvRow) {
        this.dataSource = csvRow[0];
        this.campaign = csvRow[1];
        this.daily = new Date();
        this.clicks = 0L;
        this.impressions = 0L;
    }
}
