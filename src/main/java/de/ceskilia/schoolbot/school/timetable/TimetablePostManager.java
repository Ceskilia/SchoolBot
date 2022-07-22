package de.ceskilia.schoolbot.school.timetable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TimetablePostManager {

    @NotNull List<TimetablePost> getPosts();

   default @NotNull Optional<Long> getMessageId(@NotNull LocalDate date, long guildId) {
        return getPosts().stream()
                .filter(post -> post.getTimetable().getFormattedDate().equals(date))
                .flatMap(post -> post.getGuildMessageIds().entrySet().stream()
                        .filter(entry -> entry.getKey() == guildId)
                        .map(Map.Entry::getValue))
                .findAny();
    }

    void upsertPost(@NotNull LocalDate date, long guildId);

    void broadcastTimetable(@Nullable Timetable timetable, long guildId);

    void broadcastTimetable(@Nullable Timetable timetable);

}