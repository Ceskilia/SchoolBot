package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.config.data.ConfigDataObject;
import de.ceskilia.config.data.SerializableConfigData;
import de.ceskilia.schoolbot.util.lang.ImageUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.regex.Pattern;

public interface Timetable extends SerializableConfigData {

    /**
     * The pattern of a timetable {@link #getDate() title/date}.
     */
    Pattern TITLE_PATTERN = Pattern.compile(".+, \\d{1,2}. .+ \\d{4} \\(.+\\)");

    /**
     * The creation time in milliseconds a timetable needs to be updatable.
     */
    long UPDATE_INTERVAL = 600_000; // 10min

    /**
     * Returns the title of this timetable which is also the date. {@link #TITLE_PATTERN} is used for pattern matching.
     *
     * <br> Example: Freitag, 7. Januar 2022 (A-Woche).
     *
     * @return the title/date of the timetable
     */
    @NotNull String getDate();

    /**
     * Returns the formatted version of the {@link #getDate() title/date}.
     *
     * @return a formatted date
     */
    @NotNull LocalDate getFormattedDate();

    /**
     * Returns the last time this timetable was modified on the website.
     *
     * @return the last website change time
     */
    @NotNull LocalDateTime getLastChange();

    /**
     * Returns the timestamp in milliseconds this timetable instance was created at.
     *
     * @return a timestamp of this instance creation
     */
    long getCreationTime();

    /**
     * Returns a sorted list of the absent classes.
     *
     * <br> Example: 8E, 9D, 9E, 9F, 9G, 10F, 11, 12
     *
     * @return the sorted absent classes
     */
    @NotNull List<String> getAbsentClasses();

    /**
     * Returns a sorted list of the changed classes.
     *
     * <br> Example: 8E, 9D, 9E, 9F, 9G, 10F, 11, 12
     *
     * @return the sorted changed classes
     */
    @NotNull List<String> getChangedClasses();

    /**
     * Returns a list of the given extra information. The term "Achtung!" is omitted as it usually is the heading.
     *
     * <br> Example:
     * <br> 0. Achtung!    //omitted
     * <br> 1. Klasse 9A: Keine Teilnahme am Wahlpflicht-Unterricht
     *
     * @return the extra information
     */
    @NotNull List<String> getExtraInformation();

    /**
     * Returns a list of the {@link Lesson lessons}.
     *
     * @return a list of the lessons
     */
    @NotNull List<Lesson> getLessons();

    /**
     * Returns true, if this timetable instance has at least one {@link Lesson}.
     *
     * @return true if the timetable has lessons
     * @see #getLessons()
     */
    default boolean hasLessons() {
        return !getLessons().isEmpty();
    }

    /**
     * Returns the rendered picture with the {@link #getLessons() lessons} of this timetable instance.
     * If this timetable has no lessons, this returns null.
     *
     * @return a picture of the lessons
     * @see Lesson
     * @see ImageUtil#createTimetableImage(List)
     * @see #hasLessons()
     */
    BufferedImage getImage();

    /**
     * Returns true, if this timetable instance can be updated. {@link #isOld() Old} timetables cannot be updated. It also can
     * not be updated, when the {@link #getCreationTime() creation time} is less than the {@link #UPDATE_INTERVAL}.
     *
     * @return true if the timetable can be updated
     */
    default boolean isUpdatable() {
        return !isOld() && System.currentTimeMillis() - getCreationTime() <= UPDATE_INTERVAL;
    }

    /**
     * Returns true if this timetable's {@link #getFormattedDate() date} is old.
     *
     * @return true if the timetable is old
     */
    default boolean isOld() {
        return getFormattedDate().isBefore(LocalDate.now());
    }

    interface Lesson {

        /**
         * Creates a new {@link LessonImpl lesson} based on the provided data.
         *
         * @param data the data to build the lesson with
         * @return a new lesson implementation
         */
        static @NotNull Timetable.Lesson create(@NotNull ConfigDataObject data) {
            return new LessonImpl(data);
        }

        /**
         * Returns the course/class of this lesson.
         *
         * <br> Example: 10A
         *
         * @return the course of the lesson
         */
        @NotNull String getCourse();

        /**
         * Returns the block/hours of this lessons in which the change occurred.
         *
         * <br> Example: 1-2
         *
         * @return the block of the lesson
         */
        @NotNull String getHours();

        /**
         * Returns the extra information for this lesson explaining the change and what happened.
         *
         * <br> Example: Sp xy fällt aus
         *
         * @return the extra information for the lesson
         */
        @NotNull String getInformation();

        /**
         * Returns the subject of this lesson.
         *
         * <br> Example: Sp
         *
         * @return the subject of the lesson
         */
        @NotNull String getSubject();

        /**
         * Returns the abbreviation of the teacher of this lesson.
         *
         * <br> Example: xy
         *
         * @return the teacher of the lesson
         */
        @NotNull String getTeacher();

        /**
         * Returns the room of this lesson. May be changed and may be not numerical!
         *
         * @return the room of the lesson
         */
        @NotNull String getRoom();

        /**
         * Returns true if this lesson is cancelled. The {@link #getSubject() subject} may be empty!
         *
         * @return true if the lesson is cancelled
         */
        boolean isCancelled();

    }

}
