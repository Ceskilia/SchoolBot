package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.DiscordBot;
import de.ceskilia.cutils.command.CommandConfiguration;
import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.command.slashcommand.option.Option;
import de.ceskilia.cutils.event.command.slash.GuildSlashCommandExecuteEvent;
import de.ceskilia.cutils.util.lang.ObjectUtil;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.school.channel.BroadcastChannelManager;
import de.ceskilia.schoolbot.school.channel.ChannelEntry;
import de.ceskilia.schoolbot.school.channel.TimeModifyResult;
import de.ceskilia.schoolbot.util.Emote;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import de.ceskilia.schoolbot.util.lang.ConfigUtil;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;

public class SetupCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull CommandConfiguration<GuildSlashCommandExecuteEvent> buildConfiguration(@NotNull DiscordBot unused) {
        return SlashCommandConfiguration.guildOnly("setup", "Einstellungen für den Broadcast-Channel")
                .option(Option.ofChannel("channel", "Der neue Broadcast-Channel"))
                .option(Option.ofString("time", "Die Zeit zum Updaten der Nachricht"))
                .setPermissions(DefaultMemberPermissions.DISABLED)
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final SchoolBot bot = event.getBot().cast(SchoolBot.class);
        final BroadcastChannelManager channelManager = bot.getChannelManager();

        final Guild guild = event.getGuild();
        final OptionMapping timeOption = event.getOption("time");
        final OptionMapping channelOption = event.getOption("channel");

        if (timeOption != null && channelOption != null) {
            event.replyEmbeds(entryRespond(channelManager, channelOption, timeOption, guild.getIdLong()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (timeOption != null) {
            event.replyEmbeds(timeRespond(channelManager, timeOption, guild.getIdLong()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        if (channelOption != null) {
            event.replyEmbeds(channelRespond(channelManager, channelOption, guild.getIdLong()))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        final ChannelEntry entry = channelManager.getEntry(guild.getIdLong());
        final TextChannel channel = ObjectUtil.requireNonNullOrElse(entry, null, safeEntry -> guild.getTextChannelById(safeEntry.getChannelId()));

        event.replyEmbeds(MessageUtil.embed(
                EmbedColor.INFORMATION,
                Emote.MEGA_PHONE.append("| Broadcast-Channel"),
                ConfigUtil.formatChannelInformation(entry, channel))
        ).setEphemeral(true).queue();
    }

    private @NotNull MessageEmbed entryRespond(@NotNull BroadcastChannelManager channelManager,
                                               @NotNull OptionMapping channelOption,
                                               @NotNull OptionMapping timeOption,
                                               long guildId) {
        return EmbedUtil.combineFields(channelRespond(channelManager, channelOption, guildId), timeRespond(channelManager, timeOption, guildId))
                .build();
        // currently, this is doing two I/O operations -> optimize this later
        // if both fail -> do nothing
        // if one fails -> do only for the correct one
        // if no-one fails -> do for both
    }

    private @NotNull MessageEmbed channelRespond(@NotNull BroadcastChannelManager channelManager, @NotNull OptionMapping option, long guildId) {

        final GuildChannel channel = option.getAsChannel().asGuildMessageChannel();
        final EmbedBuilder builder = new EmbedBuilder();

        EmbedUtil.addInput(builder, channel.getAsMention());

        if (!channel.getType().isMessage()) {
            return EmbedUtil.addResponse(builder, MarkdownUtil.codeblock("Falscher Channeltype"), EmbedColor.WARNING)
                    .build();
        }

        channelManager.modifyEntry(guildId, channel.getIdLong());
        return EmbedUtil.addResponse(builder, MarkdownUtil.codeblock("Geändert"), EmbedColor.SUCCESS)
                .build();
    }

    private @NotNull MessageEmbed timeRespond(@NotNull BroadcastChannelManager channelManager, @NotNull OptionMapping option, long guildId) {

        final LocalTime time = DateUtil.toTime(option.getAsString());
        final EmbedBuilder builder = new EmbedBuilder();

        EmbedUtil.addInput(builder, option.getAsString());

        if (time == null) {
            return EmbedUtil.addResponse(builder, MarkdownUtil.codeblock("Invalides Format"), EmbedColor.FAILURE)
                    .build();
        }

        final TimeModifyResult result = channelManager.modifyEntry(guildId, time);
        final EmbedColor color = result != TimeModifyResult.FAILED ? EmbedColor.SUCCESS : EmbedColor.WARNING;
        final String response = switch (result) {
            case TIME_ADDED -> "Hinzugefügt";
            case TIME_REMOVED -> "Entfernt";
            case FAILED -> "Zu nah (" + TimeUnit.MILLISECONDS.toMinutes(ChannelEntry.MINIMUM_TIME_INTERVAL) + "min)";
        };

        return EmbedUtil.addResponse(builder, MarkdownUtil.codeblock(response), color)
                .build();
    }

}