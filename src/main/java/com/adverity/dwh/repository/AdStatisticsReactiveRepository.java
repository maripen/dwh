package com.adverity.dwh.repository;

import com.adverity.dwh.model.AdStatistics;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdStatisticsReactiveRepository extends ReactiveMongoRepository<AdStatistics, String> {

}
