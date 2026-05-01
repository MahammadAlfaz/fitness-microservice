package com.fitness.ActivityService.service;

import com.fitness.ActivityService.dto.ActivityRequest;
import com.fitness.ActivityService.dto.ActivityResponse;
import com.fitness.ActivityService.model.Activity;
import com.fitness.ActivityService.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserValidationService validationService;
    private final KafkaTemplate<String,Activity> kafkaTemplate;
    @Value("${kafka.topic.name}")
    private String topicName;


    public  ActivityResponse trackActivity(ActivityRequest request) {
        boolean validUser=validationService.validateUser(request.getUserId());
        if(!validUser){
            throw  new RuntimeException("Invalid user" + request.getUserId());
        }
        Activity activity=Activity.builder()
                .userId(request.getUserId())
                .activityType(request.getActivityType())
                .duration(request.getDuration())
                .additionalMetrics(request.getAdditionalMetrics())
                .caloriesBurned(request.getCaloriesBurned())
                .startTime(request.getStartTime())
                .build();
        Activity savedActivity=activityRepository.save(activity);
        try {

            kafkaTemplate.send(topicName, savedActivity.getUserId(),savedActivity);
        }catch (Exception e){
            e.printStackTrace();
        }
        return mapToResponse(savedActivity);

    }
    public ActivityResponse mapToResponse(Activity savedActivity) {
        ActivityResponse activityResponse=ActivityResponse.builder()
                .id(savedActivity.getId())
                .userId(savedActivity.getUserId())
                .activityType(savedActivity.getActivityType())
                .duration(savedActivity.getDuration())
                .additionalMetrics(savedActivity.getAdditionalMetrics())
                .caloriesBurned(savedActivity.getCaloriesBurned())
                .startTime(savedActivity.getStartTime())
                .createdAt(savedActivity.getCreatedAt())
                .updatedAt(savedActivity.getUpdatedAt())
                .build();
        return activityResponse;
    }
}
