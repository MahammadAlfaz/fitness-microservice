package com.fitness.aiservice.service;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.aiservice.model.Activity;
import com.fitness.aiservice.model.Recommendation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
@Service
@Slf4j
@RequiredArgsConstructor
public class ActivityAIService {
    private  final GeminiService geminiService;


    public Recommendation generateRecommendation(Activity activity){
        String prompt=createPromptForActivity(activity);
        String aiResponse=geminiService.getRecommendation(prompt);
        return processAIResponse(activity,aiResponse);
    }

    private Recommendation processAIResponse(Activity activity, String aiResponse) {
     try{
         ObjectMapper objectMapper = new ObjectMapper();
         JsonNode jsonNode = objectMapper.readTree(aiResponse);
         JsonNode textNode=jsonNode.path("candidates")
                 .get(0).
                 path("content")
                 .get("parts")
                 .get(0)
                 .path("text");

         String text=textNode.asText().replaceAll("```json\\n","")
                 .replaceAll("\\n```","")
                 .trim();
         log.info("AI response {}" ,text);

         JsonNode analysisJson=objectMapper.readTree(text);
         JsonNode textAnalysisJson=analysisJson.path("analysis");
         StringBuilder fullAnalysis=new StringBuilder();

         addAnalysisSection(fullAnalysis,textAnalysisJson,"overall","Overall: ");
         addAnalysisSection(fullAnalysis,textAnalysisJson,"pace","Pace: ");
         addAnalysisSection(fullAnalysis,textAnalysisJson,"heartRate","HeartRate: ");
         addAnalysisSection(fullAnalysis,textAnalysisJson,"caloriesBurned","Calories : ");

         List<String> improvements=extractImprovements(analysisJson.path("improvements"));
         List<String> suggestions=extractSuggestion(analysisJson.path("suggestions"));
         List<String> safety=extractSaftey(analysisJson.path("safety"));

         return Recommendation.builder()
                 .activityId(activity.getId())
                 .userId(activity.getUserId())
                 .type(activity.getActivityType().toString())
                 .improvements(improvements)
                 .recommendation(fullAnalysis.toString().trim())
                 .safety(safety)
                 .suggestion(suggestions)
                 .createdAt(LocalDateTime.now())
                 .build();




     }catch (Exception e){
         e.printStackTrace();
         return createDefaultRecommendation(activity);
     }

    }

    private Recommendation createDefaultRecommendation(Activity activity) {
        return Recommendation.builder()
                .activityId(activity.getId())
                .userId(activity.getUserId())
                .type(activity.getActivityType().toString())
                .improvements(Collections.singletonList("Unable to generate"))
                .recommendation("Unable to generate")
                .safety(Collections.singletonList("Unable to generate"))
                .suggestion(Collections.singletonList("Unable to generate"))
                .createdAt(LocalDateTime.now())
                .build();
    }

    private List<String> extractSaftey(JsonNode safety) {
        List<String> safetyList=new ArrayList<>();
        if(safety.isArray()){
            safety.forEach(item->safetyList.add(item.asText()));
        }
        return safetyList.isEmpty()? Collections.singletonList("No Specific safety specified"):
                safetyList;
    }

    private List<String> extractSuggestion(JsonNode suggestions) {

        List<String> improvementsList=new ArrayList<>();
        if(suggestions.isArray()){
            suggestions.forEach(improvement->{
                String workout=improvement.path("workout").asText();
                String description=improvement.path("description").asText();
                improvementsList.add(String.format("%s,%s",workout,description));
            });
        }
        return improvementsList.isEmpty()? Collections.singletonList("No Specific improvement specified"):
                 improvementsList;
    }

    private List<String> extractImprovements(JsonNode improvements) {
        List<String> improvementsList=new ArrayList<>();
        if(improvements.isArray()){
            improvements.forEach(improvement->{
                String area=improvement.path("area").asText();
                String recommendation=improvement.path("recommendation").asText();
                improvementsList.add(String.format("%s,%s",area,recommendation));
            });
        }
        return improvementsList.isEmpty()? Collections.singletonList("No Specific improvement specified"):
                 improvementsList;
    }

    private void addAnalysisSection(StringBuilder fullAnalysis, JsonNode textAnalysisJson, String overall, String s) {
        if(!textAnalysisJson.path("overall").isMissingNode()){
            fullAnalysis.append(textAnalysisJson.path(overall).asText())
            .append("\n\n");
        }
    }

    private String createPromptForActivity(Activity activity) {

            return String.format("""
    Analyze this fitness activity and provide detailed recommendations in the EXACT JSON format shown below.

    {
    "analysis": {
    "overall": "Overall analysis here",
    "pace": "Pace analysis here",
    "heartRate": "Heart rate analysis here",
    "caloriesBurned": "Calories analysis here"
  },
  "improvements": [
    {
      "area": "Area name",
      "recommendation": "Detailed recommendation"
    }
  ],
  "suggestions": [
    {
      "workout": "Workout name",
      "description": "Detailed workout description"
    }
  ],
  "safety": [
    "Safety point 1",
    "Safety point 2"
  ]
}

    Analyze this activity:

    Activity Type: %s  
    Duration: %d minutes  
    Calories Burned: %d  
    Additional Metrics: %s  

    Provide detailed analysis focusing on performance, improvements, next workout suggestions, and safety.
    Ensure the response follows the EXACT JSON format shown above.
""",
                    activity.getActivityType(),
                    activity.getDuration(),
                    activity.getCaloriesBurned(),
                    activity.getAdditionalMetrics()
            );
        }

}
