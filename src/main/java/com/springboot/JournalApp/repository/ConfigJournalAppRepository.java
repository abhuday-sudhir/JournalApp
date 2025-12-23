package com.springboot.JournalApp.repository;

import com.springboot.JournalApp.entity.ConfigJournalAppEntity;
import com.springboot.JournalApp.entity.JournalEntry;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ConfigJournalAppRepository extends MongoRepository<ConfigJournalAppEntity, ObjectId> {

}
