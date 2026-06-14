package backend.academy.linktracker.scrapper.dto.response;

import backend.academy.linktracker.scrapper.dto.github.UserDto;
import backend.academy.linktracker.scrapper.dto.github.issue.IssuesEventPayload;
import backend.academy.linktracker.scrapper.dto.github.pr.PullRequestEventPayload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubEventResponse(
        String id,
        String type,

        UserDto actor,

        @JsonTypeInfo(
                use = JsonTypeInfo.Id.NAME,
                include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
                property = "type",
                defaultImpl = Void.class)
        @JsonSubTypes({
            @JsonSubTypes.Type(value = IssuesEventPayload.class, name = "IssuesEvent"),
            @JsonSubTypes.Type(value = PullRequestEventPayload.class, name = "PullRequestEvent"),
        })
        Object payload,

        @JsonProperty("created_at") OffsetDateTime createdAt) {}
