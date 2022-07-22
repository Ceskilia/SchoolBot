package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.Configuration;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.GuildSlashCommandExecuteEvent;
import de.ceskilia.schoolbot.util.Emote;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull Configuration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("ping","Zeit zwischen Request und Antwort")
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {
        event.deferReply(true)
                .flatMap(hook -> event.getJDA().getRestPing()
                        .flatMap(ping -> hook.sendMessageFormat(Emote.TABLE_TENNIS_PADDLE.append("%dms"), ping)))
                .queue();
    }

}
