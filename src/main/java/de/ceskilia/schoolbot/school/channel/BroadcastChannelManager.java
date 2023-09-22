package de.ceskilia.schoolbot.school.channel;

import de.ceskilia.cutils.utils.util.ObjectUtil;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.config.ArrayConfig;
import de.ceskilia.schoolbot.config.Config;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BroadcastChannelManager {

    //todo: logging
    private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastChannelManager.class);

    private final SchoolBot bot;
    private final ArrayConfig config;

    private final Set<ChannelEntry> channelEntries;

    public BroadcastChannelManager(@NotNull SchoolBot bot) {
        this.bot = bot;
        this.config = Config.arrayConfig("config/channels.json");
        this.channelEntries = fetchEntries();
    }

    public @NotNull ArrayConfig getConfig() {
        return config;
    }

    public @NotNull Set<ChannelEntry> getChannelEntries() {
        return Collections.unmodifiableSet(this.channelEntries);
    }

    public @NotNull List<ChannelEntry> getValidEntries() {
        return this.channelEntries.stream()
                .filter(entry -> entry.isValid(bot.getJDA()))
                .toList();
    }

    public @NotNull Stream<ChannelEntry> streamValidEntries() {
        return this.channelEntries.stream().filter(entry -> entry.isValid(bot.getJDA()));
    }

    public @NotNull List<TextChannel> getValidChannels() {
        return this.channelEntries.stream()
                .map(entry -> bot.getJDA().getTextChannelById(entry.getChannelId()))
                .filter(MessageUtil::canSendMessage)
                .toList();
    }

    public @Nullable TextChannel getChannelOf(long guildId) {
        return ObjectUtil.requireNonNullOrElse(getEntry(guildId), null, entry -> bot.getJDA().getTextChannelById(entry.getChannelId()));
    }

    public int countInvalidChannels() {
        return (int) this.channelEntries.stream()
                .filter(entry -> !entry.isValid(bot.getJDA()))
                .count();
    }

    public void addEntry(@Nullable ChannelEntry entry) {
        if (entry == null)
            return;
        if (this.channelEntries.add(entry))
            saveConfig(entry);
    }

    public void modifyEntry(long guildId, long channelId) {
        modifyEntry(guildId, entry -> {
            entry.setChannelId(channelId);
            return null;
        });
    }

    public @NotNull TimeModifyResult modifyEntry(long guildId, @Nullable LocalTime time) {
        return modifyEntry(guildId, entry -> entry.updateTime(time));
    }

    public @NotNull TimeModifyResult modifyEntry(long guildId, long channelId, @Nullable LocalTime time) {
        return modifyEntry(guildId, entry -> {
            entry.setChannelId(channelId);
            return entry.updateTime(time);
        });
    }

    public <T> @NotNull T modifyEntry(long guildId, @NotNull Function<? super ChannelEntry, ? extends T> function) {
        ChannelEntry entry = getEntry(guildId);

        if (entry == null) {
            final ChannelEntry newEntry = new ChannelEntry(guildId);
            final T result = function.apply(newEntry);
            addEntry(newEntry);
            return result;
        }

        final T result = function.apply(entry);
        saveConfig(entry);
        return result;
    }

    public @Nullable ChannelEntry getEntry(long guildId) {
        return optEntry(guildId).orElse(null);
    }

    public @NotNull Optional<ChannelEntry> optEntry(long guildId) {
        return this.channelEntries.stream()
                .filter(entry -> entry.getGuildId() == guildId)
                .findAny();
    }

    public @Nullable DataObject getConfigEntry(long guildId) {
        return optConfigEntry(guildId).orElse(null);
    }

    public @NotNull Optional<DataObject> optConfigEntry(long guildId) {
        return config.getData().stream(DataArray::getObject)
                .filter(element -> element.getLong("guildId") == guildId)
                .findAny();
    }

    private void saveConfig(@Nullable ChannelEntry entry) {

        if (entry != null) {
            optConfigEntry(entry.getGuildId()).ifPresentOrElse(
                    entry::fillObject,
                    () -> config.getData().add(entry.toData())
            );
            LOGGER.debug("Upserted one channel entry of guild {}.", entry.getGuildId());
        }

        config.save();
    }

    private @NotNull Set<ChannelEntry> fetchEntries() {
        return config.getData().stream(DataArray::getObject)
                .map(ChannelEntry::new)
                .collect(Collectors.toCollection(HashSet::new));
    }

}