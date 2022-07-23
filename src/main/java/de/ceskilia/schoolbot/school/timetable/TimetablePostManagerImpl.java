package de.ceskilia.schoolbot.school.timetable;

import de.ceskilia.cutils.utils.util.ThreadUtil;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TimetablePostManagerImpl implements TimetablePostManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimetablePostManagerImpl.class);

    private final SchoolBot bot;
    private final List<TimetablePost> timetablePosts;

    private int time;
    private final ScheduledExecutorService scheduler;

    public TimetablePostManagerImpl(@NotNull SchoolBot bot) {
        this.bot = bot;
        this.timetablePosts = new ArrayList<>();
        this.time = DateUtil.minutesOfDay(LocalTime.now());
        this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> ThreadUtil.toDaemon(new Thread(runnable,"Timetable-Post-Manager")));
        startScheduling();
    }

    @Override
    public @NotNull List<TimetablePost> getPosts() {
        return Collections.unmodifiableList(timetablePosts);
    }

    @Override
    public void upsertPost(@NotNull LocalDate date, long guildId) {

        final TextChannel channel = bot.getChannelManager().getChannelOf(guildId);

        if(!MessageUtil.canSendMessage(channel)) {
            return;
        }

        bot.getTimetableManager().tryUpdateTimetable(date).queue(timetable -> {

            final Optional<Long> messageId = getMessageId(date, guildId);

            if(messageId.isEmpty()) {
                broadcast(timetable, channel);
                return;
            }

            SchoolUtil.editTimetable(channel, messageId.get(), timetable, bot.getJDA().getSelfUser())
                    .thenAccept(action -> action.queue(message -> LOGGER.debug("Edited a timetable with date {} in one guild: {}", timetable.getFormattedDate(), message.getGuild().getIdLong())));
        });

    }

    @Override
    public void broadcastTimetable(@Nullable Timetable timetable, long guildId) {

        if(timetable == null) {
            return;
        }

        final TextChannel channel = bot.getChannelManager().getChannelOf(guildId);

        if(channel == null) {
            LOGGER.debug("Could not broadcast the timetable with date {} to the guild: {} (INVALID ID)", timetable.getFormattedDate(), guildId);
            return;
        }

        if(!MessageUtil.canSendMessage(channel)) {
            LOGGER.debug("Could not broadcast the timetable with date {} to the guild: {} (NO PERMISSION)", timetable.getFormattedDate(), guildId);
            return;
        }

        broadcast(timetable, channel);
    }

    @Override
    public void broadcastTimetable(@Nullable Timetable timetable) {

        if(timetable == null) {
            return;
        }

        bot.getChannelManager().getValidChannels().forEach(channel -> broadcast(timetable, channel));
    }

    private void broadcast(@NotNull Timetable timetable, @NotNull TextChannel channel) {
        SchoolUtil.sendTimetable(channel, timetable, bot.getJDA().getSelfUser())
                .thenAccept(action -> action.queue(message -> {
                    LOGGER.debug("Broadcast a timetable with date {} to one guild: {}", timetable.getFormattedDate(), message.getGuild().getIdLong());
                    findPostOfTimetable(timetable).addMessageId(message);
                }));
    }

    private @NotNull TimetablePost cachePost(@NotNull TimetablePost post) {
        this.timetablePosts.add(post);
        LOGGER.debug("Cached one timetable post with date {}.", post.getTimetable().getFormattedDate());
        return post;
    }

    private @NotNull TimetablePost findPostOfTimetable(@NotNull Timetable timetable) {
        return this.timetablePosts.stream()
                .filter(post -> post.getTimetable().getFormattedDate().equals(timetable.getFormattedDate()))
                .findAny()
                .orElse(cachePost(new TimetablePost(timetable)));
    }

    private void startScheduling() {
        LOGGER.debug("Starting timetable posting scheduler.");
        scheduler.scheduleAtFixedRate(() -> {
            this.time = (time == DateUtil.MAX_MINUTES_OF_DAY) ? 0 : time + 1;
            bot.getChannelManager().streamValidEntries()
                    .filter(entry -> entry.getUpdateTimes().stream()
                            .map(DateUtil::minutesOfDay)
                            .anyMatch(minutes -> minutes == this.time))
                    .forEach(entry -> upsertPost(LocalDate.now(), entry.getGuildId()));
        },60 - LocalTime.now().getSecond(),60,TimeUnit.SECONDS);
    }

}
