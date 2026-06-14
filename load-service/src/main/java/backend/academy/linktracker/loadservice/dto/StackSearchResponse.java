package backend.academy.linktracker.loadservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record StackSearchResponse(List<StackQuestionItem> items) {

    public record StackQuestionItem(
            @JsonProperty("question_id") long questionId) {
        public String toQuestionUrl() {
            return "https://stackoverflow.com/questions/" + questionId;
        }
    }
}
