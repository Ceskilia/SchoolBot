package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandConfiguration;
import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.command.slashcommand.option.Option;
import de.ceskilia.cutils.event.command.slash.GuildSlashCommandExecuteEvent;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.school.verification.VerificationResult;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull CommandConfiguration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("verify", "Verifiziert den aktuellen Account")
                .option(Option.ofString("username", "Der Benutzername für den Login").setRequired(true))
                .option(Option.ofString("password", "Das Password für den Login").setRequired(true))
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final SchoolBot bot = event.getBot().cast(SchoolBot.class);
        final String username = event.getRequiredOption("username", OptionMapping::getAsString);
        final String password = event.getRequiredOption("password", OptionMapping::getAsString);
        final VerificationResult result = bot.getVerificationManager().tryVerify(event.getUser().getIdLong(), username, password);

        event.replyEmbeds(buildInformation(bot, username, password, result, event.getGuild().getIdLong()))
                .setEphemeral(true)
                .queue();
    }

    private @NotNull MessageEmbed buildInformation(@NotNull SchoolBot bot,
                                                   @NotNull String username,
                                                   @NotNull String password,
                                                   @NotNull VerificationResult result,
                                                   long guildId) {
        final EmbedBuilder builder = EmbedUtil.withColor(result.isFail() ? EmbedColor.FAILURE : EmbedColor.SUCCESS);

        if (result != VerificationResult.ALREADY_VERIFIED) {
            builder.addField("Benutzername", MarkdownUtil.monospace(username), false)
                    .addField("Password", MarkdownUtil.spoiler(MarkdownUtil.monospace(password)), false);
        }

        EmbedUtil.addResponse(builder, MarkdownUtil.codeblock(result.getTranslation()));

        if (!result.isFail()) {

            final TextChannel channel = bot.getChannelManager().getChannelOf(guildId);

            if (MessageUtil.canSendMessage(channel)) {
                builder.addField("Hinweis", channel.getAsMention(), false);
            }

        }

        return builder.build();
    }

}
