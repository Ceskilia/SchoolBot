package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.schoolbot.util.lang.JsonUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import net.dv8tion.jda.api.utils.data.SerializableData;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public interface Timetable extends SerializableData {

    Pattern TITLE_PATTERN = Pattern.compile(".+, \\d{1,2}. .+ \\d{4} \\(.+\\)");

    long UPDATE_INTERVAL = 600_000; // 10min

    @NotNull String getDate();

    @NotNull LocalDate getFormattedDate();

    @NotNull String getLastChange();

    @NotNull LocalDateTime getFormattedLastChange();

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

    class Lesson {

        public static final String CANCELLED = "---";
        public static final String CHANGED_VALUE = "ae";

        private final String course;
        private final String hours;
        private final String information;
        private final String subject;
        private final String teacher;
        private final String room;

        public Lesson(@NotNull DataObject json) {
            this.course = json.getString("klasse");
            this.hours = json.getString("stunde");
            this.information = JsonUtil.safeToText(json,"info");
            this.subject = json.isType("fach", DataType.OBJECT) ? fetchValue(json.getObject("fach"),"fageaendert") : json.getString("fach");
            this.teacher = json.isType("lehrer", DataType.OBJECT) ? fetchValue(json.getObject("lehrer"),"legeaendert") : json.getString("lehrer");
            this.room = json.isType("raum", DataType.OBJECT) ? fetchValue(json.getObject("raum"),"rageaendert") : json.getString("raum");
        }

        public @NotNull String getCourse() {
            return course;
        }

        public @NotNull String getHours() {
            return hours;
        }

        public @NotNull String getInformation() {
            return information;
        }

        public @NotNull String getSubject() {
            return subject;
        }

        public @NotNull String getTeacher() {
            return teacher;
        }

        public @NotNull String getRoom() {
            return room;
        }

        public boolean isCancelled() {
            return subject.equals(CANCELLED);
        }

        private @NotNull String fetchValue(@NotNull DataObject data, @NotNull String changedKey) {
            return data.getString(changedKey).equals(CHANGED_VALUE) ? data.values().stream() // do this, because you can't parse "" as a key
                    .filter(o -> !o.equals(CHANGED_VALUE))
                    .findAny()
                    .orElse("")
                    .toString() : "ERROR";
        }

    }

}
