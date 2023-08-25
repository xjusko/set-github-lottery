package org.lottery.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RegisterForReflection(ignoreNested = false)
public record Config(
        List<Participant> participants) {

    public static final String FILE_NAME = "lottery-config.yaml";

    public record Participant(
            @JsonProperty(required = true) String user,
            @JsonProperty(required = true) int issueCount,
            @JsonProperty(required = true) Set<DayOfWeek> days,
            Optional<ZoneId> timezone
    ) {
        public boolean isReminderDay() {
            LocalDate currentDate = LocalDate.now(timezone.orElse(ZoneOffset.UTC));
            DayOfWeek currentDay = currentDate.getDayOfWeek();
            return days.contains(currentDay);
        }

        public boolean isReminderTime() {
            ZonedDateTime currentTime = ZonedDateTime.now(timezone.orElse(ZoneOffset.UTC));
            LocalTime reminderTime = LocalTime.of(10, 0);
            return currentTime.toLocalTime().equals(reminderTime)
                     || (currentTime.toLocalTime().isAfter(reminderTime));
        }
    }
}

