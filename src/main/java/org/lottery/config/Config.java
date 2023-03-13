package org.lottery.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.util.List;

@RegisterForReflection(ignoreNested = false)
public record Config(
        List<Participant> participants) {

    public static final String FILE_NAME = "lottery-config.yaml";
    public record Participant(
            @JsonProperty(required = true) String user,
            @JsonProperty(required = true) int issueCount
    ) {

    }





}
