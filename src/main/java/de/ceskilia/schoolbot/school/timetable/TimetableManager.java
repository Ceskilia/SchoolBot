package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.schoolbot.action.CompletableAction;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.CheckReturnValue;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface TimetableManager {

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

    @NotNull List<LocalDate> getAbsentDates();

    default boolean isAbsentDate(@Nullable LocalDate date) {
        return date != null && getAbsentDates().contains(date);
    }

}
