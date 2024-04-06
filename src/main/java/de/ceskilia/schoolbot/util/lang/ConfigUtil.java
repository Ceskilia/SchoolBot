package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.config.DefaultConfig;
import de.ceskilia.config.data.ConfigDataArray;
import de.ceskilia.config.data.ConfigDataObject;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.school.channel.BroadcastChannelManager;
import de.ceskilia.schoolbot.school.channel.ChannelEntry;
import de.ceskilia.schoolbot.school.timetable.TimetableManager;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import de.ceskilia.schoolbot.util.Emote;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigUtil {

    private ConfigUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static @NotNull String formatInformation(@NotNull SchoolBot bot) {
        final DefaultConfig settings = bot.getConfig();
        final VerificationManager verificationManager = bot.getVerificationManager();
        final DefaultConfig config = verificationManager.getConfig();
        final BroadcastChannelManager channelManager = bot.getChannelManager();

        return MarkdownUtil.quoteBlock(MessageUtil.isSet(settings, "token").append("Token") + "\n" +
                MessageUtil.isNotBlank(settings, "timetableURL").append("Website-URL") + "\n" +
                (!verificationManager.isAuthorized() ? Emote.WARNING : MessageUtil.isSet(config, VerificationManager.USERNAME)).append("Username") + "\n" +
                (!verificationManager.isAuthorized() ? Emote.WARNING : MessageUtil.isSet(config, VerificationManager.PASSWORD)).append("Password") + "\n" +
                MarkdownUtil.monospace(String.valueOf(config.getData().getArray(VerificationManager.VERIFIED_USERS).length())) + " Verified User" + "\n" +
                MarkdownUtil.monospace(channelManager.getChannelEntries().size() + " (" + channelManager.countInvalidChannels()) + ") Broadcast-Channel"
        );
    }

    public static @NotNull String formatChannelInformation(@Nullable ChannelEntry entry, @Nullable TextChannel channel) {
        final StringBuilder builder = new StringBuilder()
                .append(MarkdownUtil.quote("Channel - "));

        if (channel != null) {
            final Member selfMember = channel.getGuild().getSelfMember();
            final boolean hasAccess = selfMember.hasAccess(channel);
            final boolean hasPermission = selfMember.hasPermission(channel, Permission.MESSAGE_SEND);

            builder.append(channel.getAsMention());

            if (!hasAccess) {
                builder.append(Emote.RED_CROSS.append("access"));

                if(!hasPermission) {
                    builder.append(", ");
                }

            }

            if (!hasPermission) {
                builder.append(Emote.RED_CROSS.append("permission"));
            }

        } else {
            builder.append(MarkdownUtil.monospace("/"));
        }

        builder.append("\n").append(MarkdownUtil.quote("Update Zeiten - "));

        if (entry != null && !entry.getUpdateTimes().isEmpty()) {
            final String times = String.join(", ", entry.streamUpdateTimes()
                    .map(MarkdownUtil::monospace)
                    .toList());

            builder.append(times);
        } else {
            builder.append(MarkdownUtil.monospace("/"));
        }

        return builder.toString();
    }

    public static @NotNull MessageEmbed buildWithInformation(@NotNull EmbedBuilder builder, @NotNull SchoolBot bot) {
        final ConfigDataObject settings = bot.getConfig().getData();
        final ConfigDataObject verification = bot.getVerificationManager().getConfig().getData();
        final ConfigDataArray channels = bot.getChannelManager().getConfig().getData();
        final TimetableManager timetableManager = bot.getTimetableManager();

        return builder.addField("Website-URL", settings.getString("timetableURL"), false)
                .addField("Total Requests", String.valueOf(timetableManager.getTotalRequests()), false)
                .addField("Ratelimit", timetableManager.getGlobalRateLimit().toString(), false)
                .addField("Blacklisted Users", JsonUtil.toPrettyText(verification.getArray(VerificationManager.BLACKLISTED_USERS)), false)
                .addField("Verified Users", JsonUtil.toPrettyText(verification.getArray(VerificationManager.VERIFIED_USERS)), false)
                .addField("Broadcast-Channels", JsonUtil.toPrettyText(channels), false)
                .build();
    }

}
