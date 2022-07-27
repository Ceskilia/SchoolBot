package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.Configuration;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.GuildSlashCommandExecuteEvent;
import de.ceskilia.cutils.interaction.impl.Interactions;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import de.ceskilia.schoolbot.util.Emote;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import de.ceskilia.schoolbot.util.lang.ConfigUtil;
import de.ceskilia.schoolbot.util.lang.MessageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class OwnerCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull Configuration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("owner","Einstellungen für den Bot")
                .subcommand(new SubcommandData("info","Zeigt Informationen über aktuelle Einstellungen"))
                .subcommand(new SubcommandData("permission","Verändert die Interaktionsrechte eines Benutzer")
                        .addOption(OptionType.USER,"target","Der auszuwählende Benutzer",true))
                .setPermissions(DefaultMemberPermissions.DISABLED)
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final SchoolBot bot = event.getBot().cast(SchoolBot.class);

        if(Arrays.stream(bot.getAuthors()).noneMatch(author -> author.getId() == event.getUser().getIdLong())) {
            event.reply(MarkdownUtil.quote("Dafür hast du keine Berechtigung."))
                    .setEphemeral(true)
                    .queue();
            return;
        }

        final String subcommand = event.getSubcommandName();

        if(subcommand == null) {
            return;
        }

        switch (subcommand) {
            case "info" -> event.replyEmbeds(MessageUtil.embed(
                    EmbedColor.INFORMATION,
                    Emote.OPEN_FOLDER.append("| Einstellungen"),
                    ConfigUtil.formatInformation(bot))
            ).addActionRow(Interactions.button(ButtonStyle.PRIMARY,event.getUser().getId(),"Daten anfordern", Emote.ENVELOPE_ARROW.asEmoji())
                    .onClick(clickEvent -> clickEvent.replyEmbeds(ConfigUtil.buildWithInformation(EmbedColor.INFORMATION.withEmbedBuilder(), bot))
                            .setEphemeral(true)
                            .queue())
                    .queueBuild(bot)
            ).setEphemeral(true).queue();
            case "permission" -> {

                final EmbedBuilder builder = EmbedColor.SUCCESS.withEmbedBuilder();
                final VerificationManager verificationManager = bot.getVerificationManager();
                final User target = event.getRequiredOption("target",OptionMapping::getAsUser);

                EmbedUtil.addInput(builder,target.getAsMention());
                EmbedUtil.addResponse(builder,MarkdownUtil.codeblock(verificationManager.updateUserBlacklist(target.getIdLong()) ? "Hinzugefügt" : "Entfernt"));

                event.replyEmbeds(builder.build()).setEphemeral(true).queue();
            }
        }

    }

}
