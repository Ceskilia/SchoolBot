package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandConfiguration;
import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.slash.SlashCommandExecuteEvent;
import de.ceskilia.schoolbot.util.Emote;
import org.jetbrains.annotations.NotNull;

public class PingCommand implements CommandDiscriptor<SlashCommandExecuteEvent> {

    @Override
    public @NotNull CommandConfiguration<SlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.global("ping", "Zeit zwischen Request und Antwort")
                .build(this);
    }

    @Override
    public void execute(@NotNull SlashCommandExecuteEvent event) {
        event.deferReply(true)
                .flatMap(hook -> event.getJDA().getRestPing()
                        .flatMap(ping -> hook.sendMessageFormat(Emote.TABLE_TENNIS_PADDLE.append("%dms"), ping)))
                .queue();
    }

}
