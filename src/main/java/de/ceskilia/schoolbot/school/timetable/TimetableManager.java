package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.schoolbot.action.CompletableAction;
import de.ceskilia.schoolbot.school.timetable.post.TimetablePostManager;
import de.ceskilia.schoolbot.school.timetable.util.AbsentDateInformation;
import de.ceskilia.schoolbot.school.timetable.ratelimit.RateLimit;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

public interface TimetableManager {

    int getTotalRequests();

    @NotNull TimetablePostManager getPostingManager();

    @NotNull Set<Timetable> getTimetables();

    default @NotNull Set<Timetable> getOldTimetables() {
        return getTimetables().stream()
                .filter(Timetable::isOld)
                .collect(Collectors.toSet());
    }

    default @Nullable Timetable getTimetable(@NotNull LocalDate date) {
        return getTimetables().stream()
                .filter(timetable -> timetable.getFormattedDate().equals(date))
                .findFirst()
                .orElse(null);
    }

    @CheckReturnValue
    @NotNull CompletableAction<DataObject> retrieveData(@NotNull LocalDate date);

    @CheckReturnValue
    @NotNull CompletableAction<Timetable> tryUpdateTimetable(@NotNull LocalDate date);

    @CheckReturnValue
    @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date);

    @CheckReturnValue
    default @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date, boolean update) {
        return update ? tryUpdateTimetable(date) : retrieveTimetable(date);
    }

    @NotNull RateLimit getGlobalRateLimit();

    @NotNull AbsentDateInformation getAbsentDateInformation();

    default boolean isAbsentDate(@Nullable LocalDate date) {

        if(date == null) {
            return false;
        }

        final AbsentDateInformation information = getAbsentDateInformation();

        return information.isPresent() && information.getDates().contains(date);
    }

    default boolean isValidRequestDate(@Nullable LocalDate date) {
        return !DateUtil.isWeekend(date) && DateUtil.inMinimumRange(date) && !isAbsentDate(date);
    }

}
