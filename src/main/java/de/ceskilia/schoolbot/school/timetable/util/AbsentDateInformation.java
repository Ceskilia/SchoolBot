package de.ceskilia.schoolbot.school.timetable.util;

import de.ceskilia.schoolbot.util.SystemInfo;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class AbsentDateInformation {

    private List<Object> data;
    private List<LocalDate> dates;

    public static @NotNull AbsentDateInformation empty() {
        return fromData(null);
    }

    public static @NotNull AbsentDateInformation fromData(@Nullable DataObject data) {
        return new AbsentDateInformation(data);
    }

    private AbsentDateInformation(@Nullable DataObject data) {
        loadData(data);
    }

    public boolean isPresent() {
        return dates != null && !data.isEmpty();
    }

    public @NotNull List<LocalDate> getDates() {
        checkPresence();
        return Collections.unmodifiableList(dates);
    }

    public boolean containsDate(@Nullable LocalDate date) {
        return dates.contains(date);
    }

    public boolean loadData(@Nullable DataObject data) {

        if (data == null || !data.hasKey("freietage")) {
            return false;
        }

        final DataArray absentDates = data.getObject("freietage").getArray("ft");

        // if the data did not change, we don't want to do anything
        if (absentDates.toList().equals(this.data)) {
            return false;
        }

        this.data = absentDates.toList();
        this.dates = absentDates.stream(DataArray::getString)
                .map(date -> LocalDate.parse(SystemInfo.currentYearsPrefix() + date, DateTimeFormatter.BASIC_ISO_DATE))
                .collect(Collectors.toCollection(LinkedList::new));
        return true;
    }

    private void checkPresence() {
        if (dates == null) {
            throw new IllegalStateException("The date information need to be loaded first.");
        }
    }

}
