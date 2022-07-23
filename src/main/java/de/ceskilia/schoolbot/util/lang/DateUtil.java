package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.cutils.utils.util.NumberUtil;
import de.ceskilia.schoolbot.school.timetable.Timetable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.regex.Pattern;

public final class DateUtil {

    public static final int MAX_MINUTES_OF_DAY = 1439; // a day can have 1439 minutes which means 23:59

    private static final String TIME_REGEX = "([0-1]\\d|2[0-3]):([0-5]\\d)";
    private static final Pattern DATE_TIME_PATTERN = Pattern.compile("([0-3]\\d).([0-1]\\d).\\d{4}, " + TIME_REGEX);
    private static final Pattern TIME_PATTERN = Pattern.compile(TIME_REGEX);
    private static final DateTimeFormatter TITLE_FORMATTER = DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN);

    private DateUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static int minutesOfDay(@NotNull LocalTime time) {
        return time.get(ChronoField.MINUTE_OF_DAY);
    }

    public static boolean isWeekend(@Nullable LocalDate date) {
        return date != null && date.getDayOfWeek().getValue() >= 6;
    }

    public static boolean isValidDay(long day) {
        return NumberUtil.inRange(day,1,31);
    }

    public static @NotNull String formatDate(@NotNull LocalDate date) {
        return String.format("%s.%s.%s", date.getDayOfMonth(), date.getMonthValue(), date.getYear());
    }

    public static @Nullable LocalDate assumeDate(int day) {

        if(!isValidDay(day)) {
            return null;
        }

        final LocalDate now = LocalDate.now();
        final Month month = now.getMonth();
        int year = now.getYear();
        int currentDay = now.getDayOfMonth();

        if(currentDay == day) {
            return now;
        }

        int dayOfWeek = now.getDayOfWeek().getValue();
        int min = currentDay - 14;                                                 // assume the latest timetable is 2 weeks ago
        int max = currentDay + (5 - dayOfWeek) + (isUpdated(dayOfWeek) ? 7 : 0);   // assume update every thursday at ~ 6:30 am
        boolean lowerBound = min < 0;
        boolean upperBound = currentDay > month.maxLength();

        if(lowerBound && upperBound) {
            throw new IllegalStateException(String.format("The range between the current day is invalid. (day=%s) (mix=%s) (max=%s)",
                    currentDay,
                    min,
                    max
            ));
        }

        if(lowerBound && day >= month.minus(1).maxLength() + min) {
            return LocalDate.of(year - (month == Month.JANUARY ? 1 : 0), month.minus(1), day);
        }

        return day <= max - month.maxLength() ? // upper bound
                LocalDate.of(year, month.plus(1), day)
                :
                day >= min && day <= max ?
                        LocalDate.of(year, month, day) // nothing
                        :
                        null; // edge case
    }

    public static @NotNull LocalDate toDate(@NotNull String text) {

        if(!Timetable.TITLE_PATTERN.matcher(text.trim()).matches()) {
            throw new IllegalArgumentException("The provided text is formatted incorrectly: " + text);
        }

        return LocalDate.from(TITLE_FORMATTER.parse(text.substring(0, text.indexOf('(')).trim()));
    }

    public static @Nullable LocalTime toTime(@NotNull String text) {
        text = toBasicTime(text);
        return TIME_PATTERN.matcher(text).matches() ? LocalTime.parse(text) : null;
    }

    public static @NotNull LocalDateTime toDateTime(@NotNull String text) {

        if(!DATE_TIME_PATTERN.matcher(text.trim()).matches()) {
            throw new IllegalArgumentException("The provided text is formatted incorrectly: " + text);
        }

        final String[] content = text.split(", ");
        final LocalDate date = LocalDate.from(DateTimeFormatter.ofPattern("dd.MM.yyyy").parse(content[0]));
        final LocalTime time = LocalTime.parse(content[1], DateTimeFormatter.ISO_LOCAL_TIME);

        return LocalDateTime.of(date, time);
    }

    private static @NotNull String toBasicTime(@NotNull String text) {
        text = text.trim();
        return text.length() == 4 ? 0 + text : text;
    }

    private static boolean isUpdated(int dayOfWeek) {
        final LocalTime time = LocalTime.now();
        return dayOfWeek > 3 && time.getHour() > 6 && time.getMinute() > 30;
    }

}
