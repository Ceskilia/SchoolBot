package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.DiscordBot;
import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.Configuration;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.GuildSlashCommandExecuteEvent;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.Emote;
import org.jetbrains.annotations.NotNull;

public class InfoCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull Configuration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("info","Zeigt informationen über den Bot")
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final DiscordBot bot = event.getBot();

        // TODO: do this fancy with inline: true and things like that

        event.replyEmbeds(EmbedColor.INFORMATION.withEmbedBuilder()
                        .setTitle(Emote.INFORMATION.append("| Informationen"))
                        .addField("Version", bot.getVersion(),false)
                        .addField("Autoren",null,false) // todo: Autor#getAsHyperlink
                        .build())
                .queue();
    }

    //todo: info about commands, bot, website requests...

}
