package com.adverity.dwh.config;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class SimpleMongoConfig {

    @Value("${spring.data.mongodb.host}")
    private String mongoHost;

    @Value("${spring.data.mongodb.port}")
    private String mongoPort;

    @Value("${spring.data.mongodb.database}")
    private String mongoDB;

    @Value("${spring.data.mongodb.username}")
    private String mongoUser;

    @Value("${spring.data.mongodb.password}")
    private String mongoPw;

    @Bean
    public MongoClient mongo() {
        ConnectionString connectionString = new ConnectionString("mongodb://" + mongoHost +":" + mongoPort + "/" + mongoDB);
        MongoClientSettings mongoClientSettings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .credential(MongoCredential.createCredential(mongoUser, mongoDB, mongoPw.toCharArray()))
                .build();

        return MongoClients.create(mongoClientSettings);
    }

    @Bean
    public MongoTemplate mongoTemplate() throws Exception {
        return new MongoTemplate(mongo(), mongoDB);
    }
}