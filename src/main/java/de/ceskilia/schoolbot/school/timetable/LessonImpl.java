package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.schoolbot.util.lang.JsonUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.data.DataType;
import org.jetbrains.annotations.NotNull;

public class LessonImpl implements Timetable.Lesson {

    public static @NotNull Timetable.Lesson create(@NotNull DataObject data) {
        return new LessonImpl(data);
    }

    public static final String CANCELLED = "---";
    public static final String CHANGED_VALUE = "ae";

    private final String course;
    private final String hours;
    private final String information;
    private final String subject;
    private final String teacher;
    private final String room;

    public LessonImpl(@NotNull DataObject data) {
        this.course = data.getString("klasse");
        this.hours = data.getString("stunde");
        this.information = JsonUtil.safeToText(data, "info");
        this.subject = data.isType("fach", DataType.OBJECT) ? fetchValue(data.getObject("fach"), "fageaendert") : data.getString("fach");
        this.teacher = data.isType("lehrer", DataType.OBJECT) ? fetchValue(data.getObject("lehrer"), "legeaendert") : data.getString("lehrer");
        this.room = data.isType("raum", DataType.OBJECT) ? fetchValue(data.getObject("raum"), "rageaendert") : data.getString("raum");
    }

    @Override
    public @NotNull String getCourse() {
        return course;
    }

    @Override
    public @NotNull String getHours() {
        return hours;
    }

    @Override
    public @NotNull String getInformation() {
        return information;
    }

    @Override
    public @NotNull String getSubject() {
        return subject;
    }

    @Override
    public @NotNull String getTeacher() {
        return teacher;
    }

    @Override
    public @NotNull String getRoom() {
        return room;
    }

    @Override
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
