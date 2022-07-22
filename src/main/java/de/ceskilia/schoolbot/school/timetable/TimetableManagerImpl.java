package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.action.CompletableAction;
import de.ceskilia.schoolbot.action.CompletableActionImpl;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.JsonUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.CheckReturnValue;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

public class TimetableManagerImpl implements TimetableManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetableManagerImpl.class);

    private final SchoolBot bot;
    private final Set<Timetable> timetables;

    private List<LocalDate> absentDates;

    private final TimetablePostManager postManager;

    public TimetableManagerImpl(@NotNull SchoolBot bot) {
        this.bot = bot;
        this.timetables = new HashSet<>();
        this.postManager = new TimetablePostManagerImpl(bot);
    }

    public @NotNull TimetablePostManager getPostingManager() {
        return postManager;
    }

    @Override
    public @NotNull Set<Timetable> getTimetables() {
        return Collections.unmodifiableSet(this.timetables);
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<DataObject> retrieveData(@NotNull LocalDate date) {
        Checks.check(isValid(date), "The provided date is invalid. (%s)".formatted(date));
        return new CompletableActionImpl<>(bot.getHttpClient(),
                formatUrl(date),
                null,
                response -> {
                    try {
                        final DataObject data = JsonUtil.convertXmlToJson(response.body().string());

                        setupAbsentDays(data);
                        cacheTimetable(data);
                        return data;
                    } catch (final IOException e) {
                        throw new IllegalStateException("Could parse xml to json correctly.", e);
                    }
                }
        );
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<Timetable> tryUpdateTimetable(@NotNull LocalDate date) {
        final Timetable timetable = getTimetable(date);
        return retrieveTimetable(date, timetable == null || timetable.isUpdatable() ? null : timetable);
    }

    @Override
    @CheckReturnValue
    public @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date) {
        return retrieveTimetable(date, getTimetable(date));
    }

    @CheckReturnValue
    private @NotNull CompletableAction<Timetable> retrieveTimetable(@NotNull LocalDate date, @Nullable Timetable defaultValue) {
        Checks.check(isValid(date), "The provided date is invalid. (%s)".formatted(date));
        return new CompletableActionImpl<>(bot.getHttpClient(),
                formatUrl(date),
                defaultValue,
                response -> {
                    try {
                        final Timetable timetable = new TimetableImpl(response.body().string());

                        setupAbsentDays(timetable.toData());
                        return cacheTimetable(timetable);
                    } catch (final IOException e) {
                        throw new IllegalStateException("Could parse xml to json correctly.", e);
                    }
                }
        );
    }

    @Override
    public @NotNull List<LocalDate> getAbsentDates() {
        return this.absentDates != null ? Collections.unmodifiableList(this.absentDates) : Collections.emptyList();
    }

    private @NotNull Timetable cacheTimetable(@NotNull Timetable timetable) {
        this.timetables.removeIf(cachedTimetable -> cachedTimetable.getFormattedDate().equals(timetable.getFormattedDate()));
        this.timetables.add(timetable);
        LOGGER.debug("Cached one timetable with date {}.", timetable.getFormattedDate());
        return timetable;
    }

    private @NotNull Timetable cacheTimetable(@NotNull DataObject object) {
        return cacheTimetable(new TimetableImpl(object));
    }

    private @NotNull String formatUrl(@NotNull LocalDate date) {
        return String.format(bot.getConfig().retrieveData().getString("timetableURL"), date);
    }

    private boolean isValid(@Nullable LocalDate date) {
        return !DateUtil.isWeekend(date) && !isAbsentDate(date);
    }

    private void setupAbsentDays(@NotNull DataObject data) {
        if(absentDates == null || absentDates.isEmpty()) {
            this.absentDates = SchoolUtil.fetchAbsentDates(data);
            LOGGER.debug("Fetched absent days successfully.");
        }
    }

}