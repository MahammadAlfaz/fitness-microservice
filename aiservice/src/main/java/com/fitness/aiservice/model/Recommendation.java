package com.fitness.aiservice.model;

import lombok.Builder;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;

@Document()
@Builder
public class Recommendation {
    @Id
    private String id;
    private String userId;
    private String activityId;
    private String type;
    private String recommendation;
    private List<String> improvements;
    private List<String> suggestion;
    private List<String> safety;
    @CreatedDate
    private LocalDateTime createdAt;


}
