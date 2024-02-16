package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.config.internal.ConfigDataObject;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.action.CompletableAction;
import de.ceskilia.schoolbot.action.CompletableActionImpl;
import de.ceskilia.schoolbot.school.timetable.post.TimetablePostManager;
import de.ceskilia.schoolbot.school.timetable.post.TimetablePostManagerImpl;
import de.ceskilia.schoolbot.school.timetable.ratelimit.RateLimit;
import de.ceskilia.schoolbot.school.timetable.ratelimit.RateLimitException;
import de.ceskilia.schoolbot.school.timetable.util.AbsentDateInformation;
import de.ceskilia.schoolbot.util.lang.JsonUtil;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class TimetableManagerImpl implements TimetableManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableManagerImpl.class);

    private final SchoolBot bot;
    private final AbsentDateInformation absentDateInformation;
    private final RateLimit rateLimit;
    private final Set<Timetable> timetables;

    private final TimetablePostManager postManager;

    private int totalRequests;

    public TimetableManagerImpl(@NotNull SchoolBot bot) {
        this.bot = bot;
        this.absentDateInformation = AbsentDateInformation.empty();
        this.rateLimit = new RateLimit(5, 1, TimeUnit.MINUTES);
        this.timetables = new HashSet<>();
        this.postManager = new TimetablePostManagerImpl(bot);
    }

    @Override
    public int getTotalRequests() {
        return totalRequests;
    }

    @Override
    public @NotNull TimetablePostManager getPostingManager() {
        return postManager;
    }

    @Override
    public @NotNull Set<Timetable> getTimetables() {
        return Collections.unmodifiableSet(this.timetables);
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<ConfigDataObject> retrieveData(@NotNull LocalDate date) {
        Checks.check(isValidRequestDate(date), "The provided date is invalid. (%s)".formatted(date));
        checkRateLimit();
        this.totalRequests++;
        return new CompletableActionImpl<>(bot.getHttpClient(),
                formatUrl(date),
                null,
                response -> {
                    try {
                        final ConfigDataObject data = JsonUtil.convertXmlToJson(response.body().string());

                        updateAbsentDates(data);
                        cacheTimetable(data);
                        return data;
                    } catch (final IOException e) {
                        throw new IllegalStateException("Could parse xml to json correctly.", e);
                    }
                }
        );
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<Timetable> tryUpdateTimetable(@NotNull LocalDate date) {
        final Timetable timetable = getTimetable(date);
        // when there is no timetable, or we can update it -> new request
        return retrieveTimetable(date, timetable == null || timetable.isUpdatable() ? null : timetable);
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date) {
        return retrieveTimetable(date, getTimetable(date));
    }

    @CheckReturnValue
    private @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date, @Nullable Timetable defaultValue) {
        Checks.check(isValidRequestDate(date), "The provided date is invalid. (%s)".formatted(date));
        checkRateLimit();
        this.totalRequests++;
        return new CompletableActionImpl<>(bot.getHttpClient(),
                formatUrl(date),
                defaultValue,
                response -> {
                    try {
                        final Timetable timetable = new TimetableImpl(response.body().string());

                        updateAbsentDates(timetable.toData());
                        return cacheTimetable(timetable);
                    } catch (final IOException e) {
                        throw new IllegalStateException("Could parse xml to json correctly.", e);
                    }
                }
        );
    }

    @Override
    public @NotNull RateLimit getGlobalRateLimit() {
        return rateLimit;
    }

    @Override
    public @NotNull AbsentDateInformation getAbsentDateInformation() {
        return absentDateInformation;
    }

    private @NotNull Timetable cacheTimetable(@NotNull ConfigDataObject object) {
        return cacheTimetable(new TimetableImpl(object));
    }

    private @NotNull Timetable cacheTimetable(@NotNull Timetable timetable) {
        this.rateLimit.registerRequest();
        this.timetables.removeIf(cachedTimetable -> cachedTimetable.getFormattedDate().equals(timetable.getFormattedDate()));
        this.timetables.add(timetable);
        LOGGER.debug("Cached one timetable with date {}.", timetable.getFormattedDate());
        return timetable;
    }

    private @NotNull String formatUrl(@NotNull LocalDate date) {
        return String.format(bot.getConfig().retrieveData().getString("timetableURL"), date);
    }

    private void updateAbsentDates(@NotNull ConfigDataObject data) {
        if (absentDateInformation.loadData(data)) {
            LOGGER.debug("Updated absent dates successfully.");
        }
    }

    private void checkRateLimit() {
        if (rateLimit.isReached()) {
            throw new RateLimitException(String.format("Too many requests in %sms (%s)", rateLimit.getDuration(), rateLimit.getMaxRequests()));
        }
    }

}