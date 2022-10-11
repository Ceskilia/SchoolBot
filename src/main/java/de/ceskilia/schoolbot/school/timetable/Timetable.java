package de.ceskilia.schoolbot.school.timetable;

import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public interface Timetable extends SerializableData {

    Pattern TITLE_PATTERN = Pattern.compile(".+, \\d{1,2}. .+ \\d{4} \\(.+\\)");

    long UPDATE_INTERVAL = 600_000; // 10min

    @NotNull String getDate();

    @NotNull LocalDate getFormattedDate();

    @NotNull LocalDateTime getLastChange();

    long getCreationTime();

    @NotNull List<String> getAbsentClasses();

    @NotNull List<String> getChangedClasses();

    @NotNull List<String> getExtraInformation();

    @NotNull List<Lesson> getLessons();

    default boolean hasLessons() {
        return !getLessons().isEmpty();
    }

    @NotNull BufferedImage getImage();

    default boolean isUpdatable() {
        return !isOld() && System.currentTimeMillis() - getCreationTime() <= UPDATE_INTERVAL;
    }

    default boolean isOld() {
        return getFormattedDate().isBefore(LocalDate.now());
    }

    interface Lesson {

        @NotNull String getCourse();

        @NotNull String getHours();

        @NotNull String getInformation();

        @NotNull String getSubject();

        @NotNull String getTeacher();

        @NotNull String getRoom();

        boolean isCancelled();

    }

}
