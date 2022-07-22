package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.Configuration;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.GuildSlashCommandExecuteEvent;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import de.ceskilia.schoolbot.school.verification.VerificationResult;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull Configuration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("verify","Verifiziert den aktuellen Account")
                .option(OptionType.STRING,"username","Der Benutzername fuer den Login",true)
                .option(OptionType.STRING,"password","Das Password fuer den Login",true)
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final SchoolBot bot = event.getBot().cast(SchoolBot.class);
        final String username = event.getOption("username").getAsString();
        final String password = event.getOption("password").getAsString();
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
        final EmbedBuilder builder = (result.isFail() ? EmbedColor.FAILURE : EmbedColor.SUCCESS).withEmbedBuilder();

        if(result != VerificationResult.ALREADY_VERIFIED) {
            builder.addField("Benutzername", MarkdownUtil.monospace(username),false)
                    .addField("Password", MarkdownUtil.spoiler(MarkdownUtil.monospace(password)),false);
        }

        builder.addField("Antwort", MarkdownUtil.codeblock(result.name()),false);

        if(!result.isFail()) {

            final TextChannel channel = bot.getChannelManager().getChannelOf(guildId);

            if(MessageUtil.canSendMessage(channel)) {
                builder.addField("Hinweis", channel.getAsMention(),false);
            }

        }

        return builder.build();
    }

}
