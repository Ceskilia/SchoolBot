package de.ceskilia.schoolbot.school.timetable.ratelimit;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimit {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private int requests;
    private final int maxRequests;
    private final long duration;

    public RateLimit(int maxRequests, long duration, @NotNull TimeUnit durationUnit) {
        this.maxRequests = maxRequests;
        this.duration = durationUnit.toMillis(duration);
    }

    public int getRequests() {
        return requests;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getDuration() {
        return duration;
    }

    public void performRequest() {

        if(requests == 0) {
            queueReset();
        }

        this.requests++;
    }

    public boolean isReached() {
        return getRequests() >= getMaxRequests();
    }

    private void queueReset() {
        SCHEDULED_EXECUTOR.schedule(() -> this.requests = 0, getDuration(), TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateLimit rateLimit = (RateLimit) o;
        return requests == rateLimit.requests && maxRequests == rateLimit.maxRequests && duration == rateLimit.duration;
    }

    @Override
    public int hashCode() {
        return Objects.hash(requests, maxRequests, duration);
    }

    @Override
    public @NotNull String toString() {
        return "Requests: " + requests +
                ", max. Requests: " + maxRequests +
                ", Duration: " + duration;
    }

}
