package org.lottery.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;

@RegisterForReflection(ignoreNested = false)
public record Config(
        List<Participant> participants) {

    public static final String FILE_NAME = "lottery-config.yaml";

    public record Participant(
            @JsonProperty(required = true) String user,
            @JsonProperty(required = true) int issueCount,
            @JsonProperty(required = true) Set<DayOfWeek> days,
            String timezone
    ) {
        public ZoneId timezoneId() {
            return timezone == null ? ZoneId.of("UTC") : ZoneId.of(timezone);
        }
    }
}

