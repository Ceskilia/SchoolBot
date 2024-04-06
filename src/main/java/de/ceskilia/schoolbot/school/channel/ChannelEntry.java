package de.ceskilia.schoolbot.school.channel;

import de.ceskilia.config.data.ConfigDataArray;
import de.ceskilia.config.data.ConfigDataObject;
import de.ceskilia.config.data.SerializableConfigData;
import de.ceskilia.schoolbot.util.lang.JsonUtil;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnusedReturnValue")
public class ChannelEntry implements SerializableConfigData {

    public static final long MINIMUM_TIME_INTERVAL = 1_800_000; //30min

    private static final String GUILD_ID = "guildId";
    private static final String CHANNEL_ID = "channelId";
    private static final String UPDATE_TIMES = "updateTimes";

    private final long guildId;
    private long channelId;
    private final SortedSet<LocalTime> updateTimes;

    public ChannelEntry(long guildId) {
        this(guildId, 0, null);
    }

    public ChannelEntry(long guildId, long channelId, @Nullable Set<LocalTime> updateTimes) {
        Checks.isSnowflake(String.valueOf(guildId), "GuildID");
        if (channelId != 0) Checks.isSnowflake(String.valueOf(channelId), "ChannelID");
        this.guildId = guildId;
        this.channelId = channelId;
        this.updateTimes = updateTimes != null ? new TreeSet<>(updateTimes) : new TreeSet<>();
    }

    public ChannelEntry(@NotNull ConfigDataObject json) {
        this.guildId = json.getLong(GUILD_ID);
        this.channelId = json.getLong(CHANNEL_ID);
        this.updateTimes = json.hasKey(UPDATE_TIMES) ? json.getArray(UPDATE_TIMES)
                .stream(ConfigDataArray::getString)
                .map(value -> LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME))
                .collect(Collectors.toCollection(TreeSet::new)) : new TreeSet<>();
    }

    public long getGuildId() {
        return guildId;
    }

    public long getChannelId() {
        return channelId;
    }

    protected void setChannelId(long channelId) {
        Checks.isSnowflake(String.valueOf(channelId), "Channel Id");
        this.channelId = channelId;
    }

    public boolean hasChannelId() {
        return channelId != 0;
    }

    public @NotNull Set<LocalTime> getUpdateTimes() {
        return Collections.unmodifiableSet(this.updateTimes);
    }

    public @NotNull Stream<String> streamUpdateTimes() {
        return updateTimes.stream().map(LocalTime::toString);
    }

    protected @NotNull TimeModifyResult updateTime(@Nullable LocalTime updateTime) {

        if (updateTime == null) {
            return TimeModifyResult.FAILED;
        }

        for (final LocalTime time : this.updateTimes) {
            if (time.equals(updateTime)) {
                updateTimes.remove(time);
                return TimeModifyResult.TIME_REMOVED;
            }
        }

        final boolean success = canAdd(updateTime) && updateTimes.add(updateTime);

        return success ? TimeModifyResult.TIME_ADDED : TimeModifyResult.FAILED;
    }

    private boolean canAdd(@Nullable LocalTime time) {

        if (time == null) {
            return false;
        }

        for (final LocalTime updateTime : updateTimes)
            if (Math.abs(updateTime.until(time, ChronoUnit.MILLIS)) < MINIMUM_TIME_INTERVAL)
                return false;
        return true;
    }

    public boolean isValid(@NotNull JDA jda) {
        return jda.getGuildById(guildId) != null && MessageUtil.canSendMessage(jda.getTextChannelById(channelId));
    }

    @Override
    public @NotNull ConfigDataObject toData() {
        return ConfigDataObject.empty()
                .put(GUILD_ID, guildId)
                .put(CHANNEL_ID, channelId)
                .put(UPDATE_TIMES, ConfigDataArray.fromCollection(streamUpdateTimes().toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChannelEntry entry = (ChannelEntry) o;
        return guildId == entry.guildId && channelId == entry.channelId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(guildId, channelId);
    }

}
