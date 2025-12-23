package com.springboot.JournalApp.entity;

import com.springboot.JournalApp.enums.Sentiment;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "journal_entries")
@Data
public class JournalEntry
{
    @Id
    private ObjectId id;

    @NonNull
    private String title;

    private String content;

    private Sentiment sentiment;

    private  LocalDateTime date;

}
