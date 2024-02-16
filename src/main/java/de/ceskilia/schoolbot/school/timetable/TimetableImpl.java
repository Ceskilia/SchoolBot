package de.ceskilia.schoolbot.school.timetable;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.ceskilia.config.internal.ConfigDataArray;
import de.ceskilia.config.internal.ConfigDataObject;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.ImageUtil;
import de.ceskilia.schoolbot.util.lang.JsonUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TimetableImpl implements Timetable {

    private final ConfigDataObject data;

    private final String date;
    private final LocalDate formattedDate;
    private final LocalDateTime formattedLastChange;

    private final List<String> absentClasses;
    private final List<String> changedClasses;
    private final List<String> extraInformation;
    private final List<Lesson> lessons;

    private final BufferedImage image;

    private final long creationTime;

    public TimetableImpl(@NotNull String xml) throws JsonProcessingException {
        this(JsonUtil.convertXmlToJson(xml));
    }

    public TimetableImpl(@NotNull ConfigDataObject data) {
        this.data = data;

        final ConfigDataObject head = data.getObject("kopf");
        final ConfigDataObject headInfo = head.optObject("kopfinfo").orElse(null);
        final ConfigDataArray main = JsonUtil.safeToArray(data.optObject("haupt").orElse(null), "aktion");

        this.date = head.getString("titel").trim();
        this.formattedDate = DateUtil.timetableTitleToDate(this.date);
        this.formattedLastChange = DateUtil.toDateTime(head.getString("datum"));
        this.absentClasses = JsonUtil.safeToList(headInfo, "abwesendk");
        this.changedClasses = JsonUtil.safeToList(headInfo, "aenderungk");
        this.extraInformation = SchoolUtil.fetchExtraInformation(data);

        Collections.sort(absentClasses);
        Collections.sort(changedClasses);

        this.lessons = main.stream(ConfigDataArray::getObject)
                .map(Lesson::create)
                .toList();
        this.image = hasLessons() ? ImageUtil.createTimetableImage(lessons) : null;
        this.creationTime = System.currentTimeMillis();
    }

    @Override
    public @NotNull String getDate() {
        return date;
    }

    @Override
    public @NotNull LocalDate getFormattedDate() {
        return formattedDate;
    }

    @Override
    public @NotNull LocalDateTime getLastChange() {
        return formattedLastChange;
    }

    @Override
    public long getCreationTime() {
        return creationTime;
    }

    @Override
    public @NotNull List<String> getAbsentClasses() {
        return absentClasses;
    }

    @Override
    public @NotNull List<String> getChangedClasses() {
        return changedClasses;
    }

    @Override
    public @NotNull List<String> getExtraInformation() {
        return extraInformation;
    }

    @Override
    public @NotNull List<Lesson> getLessons() {
        return lessons;
    }

    @Override
    public BufferedImage getImage() {
        return image;
    }

    @Override
    public @NotNull ConfigDataObject toData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimetableImpl timetable = (TimetableImpl) o;
        return creationTime == timetable.creationTime
                && data.equals(timetable.data)
                && date.equals(timetable.date)
                && formattedDate.equals(timetable.formattedDate)
                && Objects.equals(changedClasses, timetable.changedClasses)
                && extraInformation.equals(timetable.extraInformation)
                && lessons.equals(timetable.lessons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data, date, formattedDate, changedClasses, extraInformation, lessons, creationTime);
    }

}
