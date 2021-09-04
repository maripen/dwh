package com.adverity.dwh.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class AdItemsRepository {

    private MongoTemplate mongoTemplate;

    @Autowired
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
