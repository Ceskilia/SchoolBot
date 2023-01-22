package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.config.DefaultConfig;
import de.ceskilia.schoolbot.school.channel.BroadcastChannelManager;
import de.ceskilia.schoolbot.school.channel.ChannelEntry;
import de.ceskilia.schoolbot.school.timetable.TimetableManager;
import de.ceskilia.schoolbot.util.Emote;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
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
                MarkdownUtil.monospace(String.valueOf(config.retrieveData().getArray(VerificationManager.VERIFIED_USERS).length())) + " Verified User" + "\n" +
                MarkdownUtil.monospace(channelManager.getChannelEntries().size() + "|" + channelManager.countInvalidChannels()) + " Broadcast-Channel"
        );
    }

    public static @NotNull String formatChannelInformation(@Nullable ChannelEntry entry, @Nullable TextChannel channel) {

        final StringBuilder builder = new StringBuilder()
                .append(MarkdownUtil.quote("Channel - "));

        if (channel != null) {

            final Member selfMember = channel.getGuild().getSelfMember();

            builder.append(channel.getAsMention())
                    .append(" [")
                    .append(MessageUtil.check(selfMember.hasAccess(channel)).getUnicode())
                    .append("|")
                    .append(MessageUtil.check(selfMember.hasPermission(channel, Permission.MESSAGE_SEND)).getUnicode())
                    .append("]");
        } else {
            builder.append(MarkdownUtil.monospace("/"));
        }

        builder.append("\n").append(MarkdownUtil.quote("Update Zeiten - "));

        if (entry != null && !entry.getUpdateTimes().isEmpty()) {
            builder.append(String.join(", ", entry.streamUpdateTimes()
                    .map(MarkdownUtil::monospace)
                    .toList())
            );
        } else {
            builder.append(MarkdownUtil.monospace("/"));
        }

        return builder.toString();
    }

    public static @NotNull MessageEmbed buildWithInformation(@NotNull EmbedBuilder builder, @NotNull SchoolBot bot) {

        final DataObject settings = bot.getConfig().retrieveData();
        final DataObject verification = bot.getVerificationManager().getConfig().retrieveData();
        final DataArray channels = bot.getChannelManager().getConfig().retrieveData();
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
