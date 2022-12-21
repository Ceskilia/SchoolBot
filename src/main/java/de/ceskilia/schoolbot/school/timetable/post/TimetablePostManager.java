package de.ceskilia.schoolbot.school.timetable.post;

import de.ceskilia.schoolbot.school.channel.BroadcastChannelManager;
import de.ceskilia.schoolbot.school.channel.ChannelEntry;
import de.ceskilia.schoolbot.school.timetable.Timetable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface TimetablePostManager {

    /**
     * Returns a list of the {@link TimetablePost timetable posts} of this post manager.
     *
     * @return the timetable posts
     */
    @NotNull List<TimetablePost> getPosts();

    /**
     * Returns the message id of the {@link TimetablePost timetable post} with the provided date in the provided guild.
     * The value is not present when no timetable with the date and the guild id exists.
     *
     * @param date the date of the timetable
     * @param guildId the guild id the timetable was sent to
     * @return the message id of the timetable post
     */
   default @NotNull Optional<Long> getMessageId(@NotNull LocalDate date, long guildId) {
        return getPosts().stream()
                .filter(post -> post.getTimetable().getFormattedDate().equals(date))
                .flatMap(post -> post.getGuildMessageIds().entrySet().stream()
                        .filter(entry -> entry.getKey() == guildId)
                        .map(Map.Entry::getValue))
                .findAny();
    }

    /**
     * Upserts the {@link TimetablePost timetable post} with the provided date and the guild id. When no post exists,
     * this will send the timetable with the provided date in the guild with the provided id, else this updates it.
     *
     * <br> Note that the {@link BroadcastChannelManager#getChannelOf(long)} must be valid and that the bot has permission
     * <br> to send messages in it.
     *
     * @see #getMessageId(LocalDate, long)
     *
     * @param date the date of the timetable
     * @param guildId the guild id the timetable should be sent to
     */
    void upsertPost(@NotNull LocalDate date, long guildId);

    /**
     * This will send the provided {@link Timetable} to the guild with the provided id.
     *
     * <br> Note that the {@link BroadcastChannelManager#getChannelOf(long)} must be valid and that the bot has permission
     * <br> to send messages in it.
     *
     * @param timetable the timetable to send to the guild
     * @param guildId the guild id the timetable should be sent to
     */
    void broadcastTimetable(@Nullable Timetable timetable, long guildId);

    /**
     * This will send the provided {@link Timetable} to all valid guilds the bot is in. Valid means, that the guild
     * has a set {@link ChannelEntry#getChannelId() channel id} and the bot can send messages in it.
     *
     * @param timetable the timetable to send to the guild
     */
    void broadcastTimetable(@Nullable Timetable timetable);

}