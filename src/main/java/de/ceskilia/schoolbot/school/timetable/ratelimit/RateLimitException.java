package de.ceskilia.schoolbot.school.timetable.ratelimit;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RateLimitException extends RuntimeException {

    public RateLimitException(@NotNull String message) {
        super(message);
    }

    public RateLimitException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
