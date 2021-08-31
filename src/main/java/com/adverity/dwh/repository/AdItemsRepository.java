package com.adverity.dwh.repository;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AdItemsRepository {

    private MongoTemplate mongoTemplate;

    public AdItemsRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveAll(List<Map<String, Object>> adItems, String collectionName) {
        adItems.forEach( adItem -> mongoTemplate.save(adItem, collectionName));
    }

    public void deleteCollection(String collectionName) {
        this.mongoTemplate.dropCollection(collectionName);
    }
}
