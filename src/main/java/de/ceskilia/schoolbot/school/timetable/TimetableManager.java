package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.config.data.ConfigDataObject;
import de.ceskilia.schoolbot.action.CompletableAction;
import de.ceskilia.schoolbot.school.timetable.post.TimetablePost;
import de.ceskilia.schoolbot.school.timetable.post.TimetablePostManager;
import de.ceskilia.schoolbot.school.timetable.ratelimit.RateLimit;
import de.ceskilia.schoolbot.school.timetable.ratelimit.RateLimitException;
import de.ceskilia.schoolbot.school.timetable.util.AbsentDateInformation;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public interface TimetableManager {

    /**
     * Returns the amount of requests made to the school website.
     *
     * @return the amount of requests
     */
    int getTotalRequests();

    /**
     * Returns the manager for handling {@link TimetablePost timetable posts}.
     *
     * @return the manager for timetable posts
     */
    @NotNull TimetablePostManager getPostingManager();

    /**
     * Returns a set of all cached timetables. Timetables are cached after a request to the school website.
     *
     * @return all cached timetables
     * @see #retrieveData(LocalDate)
     * @see #tryUpdateTimetable(LocalDate)
     * @see #retrieveTimetable(LocalDate)
     */
    @NotNull Set<Timetable> getTimetables();

    /**
     * Returns a set of all {@link Timetable#isOld() old} cached {@link #getTimetables() timetables}.
     *
     * @return all old cached timetables
     */
    default @NotNull Set<Timetable> getOldTimetables() {
        return getTimetables().stream()
                .filter(Timetable::isOld)
                .collect(Collectors.toSet());
    }

    /**
     * Returns the cached {@link Timetable} with the specified {@link Timetable#getDate()}. Null if there is no match.
     *
     * @param date the date of the timetable
     * @return the timetable with the specified date
     * @see #getTimetables()
     */
    default @Nullable Timetable getTimetable(@NotNull LocalDate date) {
        return getTimetables().stream()
                .filter(timetable -> timetable.getFormattedDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns a {@link CompletableAction} containing the raw data of the timetable with the specified {@link Timetable#getDate() date}.
     * This caches a timetable based on the fetched data.
     *
     * @param date the date of the timetable
     * @return a completable action with the raw data of the timetable
     * @throws IllegalArgumentException if the provided date is invalid for doing a request
     * @throws RateLimitException       if the global ratelimit is reached
     * @see #isValidRequestDate(LocalDate)
     * @see #getGlobalRateLimit()
     */
    @CheckReturnValue
    @NotNull CompletableAction<ConfigDataObject> retrieveData(@NotNull LocalDate date);

    /**
     * Returns a {@link CompletableAction} containing the timetable with the specified {@link Timetable#getDate() date}.
     * This requests a new timetable, if the timetable is {@link Timetable#isUpdatable() updatable} or when no timetable
     * with the specified date exists. The fetched instance is cached.
     * Else, it returns the {@link #getTimetable(LocalDate) cached instance}.
     *
     * @param date the date of the timetable
     * @return a completable action with a timetable instance
     * @throws IllegalArgumentException if the provided date is invalid for doing a request
     * @throws RateLimitException       if the global ratelimit is reached
     * @see #isValidRequestDate(LocalDate)
     * @see #getGlobalRateLimit()
     */
    @CheckReturnValue
    @NotNull CompletableAction<Timetable> tryUpdateTimetable(@NotNull LocalDate date);

    /**
     * Returns a {@link CompletableAction} containing the timetable with the specified {@link Timetable#getDate() date}.
     * This requests a new timetable, if no timetable with the specified date exists. The fetched instance is cached.
     * Else, it returns the {@link #getTimetable(LocalDate) cached instance}.
     *
     * @param date the date of the timetable
     * @return a completable action with a timetable instance
     * @throws IllegalArgumentException if the provided date is invalid for doing a request
     * @throws RateLimitException       if the global ratelimit is reached
     * @see #isValidRequestDate(LocalDate)
     * @see #getGlobalRateLimit()
     */
    @CheckReturnValue
    @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date);

    /**
     * Returns a {@link CompletableAction} containing the timetable with the specified {@link Timetable#getDate() date}.
     * This requests a new timetable, if no timetable with the specified date exists or, when wanting to potentially
     * update the instance, the timetable is {@link Timetable#isUpdatable() updatable}. The fetched instance is cached.
     * Else, it returns the {@link #getTimetable(LocalDate) cached instance}.
     *
     * @param date   the date of the timetable
     * @param update whether to potentially update the timetable instance
     * @return a completable action with a timetable instance
     * @throws IllegalArgumentException if the provided date is invalid for doing a request
     * @throws RateLimitException       if the global ratelimit is reached
     * @see #isValidRequestDate(LocalDate)
     * @see #getGlobalRateLimit()
     */
    @CheckReturnValue
    default @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date, boolean update) {
        return update ? tryUpdateTimetable(date) : retrieveTimetable(date);
    }

    /**
     * Returns the global {@link RateLimit} for this timetable manager instance.
     * You can only do {@link RateLimit#getMaxRequests() x requests} in {@link RateLimit#getDuration() y} ms.
     *
     * @return the global ratelimit
     */
    @NotNull RateLimit getGlobalRateLimit();

    /**
     * Returns the absent date information. A date cannot be used for requesting when it is marked as absent.
     *
     * @return the absent date information
     */
    @NotNull AbsentDateInformation getAbsentDateInformation();

    /**
     * Returns true if the provided date is noted as absent.
     *
     * @param date the date to check
     * @return true if the date is absent
     * @see #getAbsentDateInformation()
     */
    default boolean isAbsentDate(@Nullable LocalDate date) {

        if (date == null) {
            return false;
        }

        final AbsentDateInformation information = getAbsentDateInformation();

        return information.isPresent() && information.containsDate(date);
    }

    /**
     * Returns true if the provided date is not noted as absent, not older than 2 weeks and not a weekend day.
     *
     * @param date the date to check
     * @return true if the date is valid for doing a request
     */
    default boolean isValidRequestDate(@Nullable LocalDate date) {
        return !DateUtil.isWeekend(date) && DateUtil.inMinimumRange(date) && !isAbsentDate(date);
    }

}
