package de.ceskilia.schoolbot.school.timetable.ratelimit;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RateLimit {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = Executors.newSingleThreadScheduledExecutor();

    private int requests;
    private final int maxRequests;
    private final long timeInterval;

    public RateLimit(int maxRequests, long timeInterval, @NotNull TimeUnit intervalUnit) {
        this.maxRequests = maxRequests;
        this.timeInterval = intervalUnit.toMillis(timeInterval);
    }

    public int getRequests() {
        return requests;
    }

    public int getMaxRequests() {
        return maxRequests;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public void performRequest() {

        if(requests == 0) {
            startResetting();
        }

        this.requests++;
    }

    public boolean isReached() {
        return getRequests() >= getMaxRequests();
    }

    private void startResetting() {
        SCHEDULED_EXECUTOR.scheduleAtFixedRate(() -> this.requests = 0, 0, getTimeInterval(), TimeUnit.MILLISECONDS);
    }

}
