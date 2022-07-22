package de.ceskilia.schoolbot.school.timetable;

import net.dv8tion.jda.api.entities.Message;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TimetablePost {

    private final Timetable timetable;
    private final Map<Long, Long> guildMessageIds;

    public TimetablePost(@NotNull Timetable timetable) {
        this.timetable = timetable;
        this.guildMessageIds = new HashMap<>();
    }

    public @NotNull Timetable getTimetable() {
        return timetable;
    }

    public @NotNull Map<Long, Long> getGuildMessageIds() {
        return Collections.unmodifiableMap(this.guildMessageIds);
    }

    public void addMessageId(long guildId, long messageId) {
        guildMessageIds.put(guildId, messageId);
    }

    public void addMessageId(@Nullable Message message) {

        if(message == null) {
            return;
        }

        addMessageId(message.getGuild().getIdLong(), message.getIdLong());
    }

    public void addMessageIds(@Nullable Collection<Message> messages) {

        if(messages == null) {
            return;
        }

        for(final Message message : messages) {
            addMessageId(message.getGuild().getIdLong(), message.getIdLong());
        }

    }

}
