package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.DiscordBot;
import de.ceskilia.cutils.command.CommandConfiguration;
import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.command.slashcommand.option.Option;
import de.ceskilia.cutils.event.command.slash.GuildSlashCommandExecuteEvent;
import de.ceskilia.cutils.event.command.slash.SlashCommandExecuteEvent;
import de.ceskilia.cutils.util.lang.ObjectUtil;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.action.ErrorResponseException;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class TimetableCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull CommandConfiguration<GuildSlashCommandExecuteEvent> buildConfiguration(@NotNull DiscordBot unused) {
        return SlashCommandConfiguration.guildOnly("timetable", "Zeigt den Vertretungsplan an")
                .option(Option.ofString("date", "Das Datum des Vertretungsplans"))
                .option(Option.ofBoolean("update", "Den angefragten Vertretungsplan aktualisieren"))
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {
        final SchoolBot bot = event.getBot().cast(SchoolBot.class);
        final VerificationManager verificationManager = bot.getVerificationManager();
        final User user = event.getUser();

        if (verificationManager.isBlacklisted(user.getIdLong())) {
            respondFailure(event, null, "Du wurdest ausgeschlossen.", EmbedColor.FAILURE);
            return;
        }

        if (!verificationManager.isVerified(user.getIdLong())) {
            respondFailure(event, null, "Bitte verifiziere dich zuerst.", EmbedColor.WARNING);
            return;
        }

        if (!verificationManager.canRequest()) {
            respondFailure(event, null, "Die letzte Anfrage konnte nicht autorisiert werden.", EmbedColor.FAILURE);
            return;
        }

        if (bot.getTimetableManager().getGlobalRateLimit().isReached()) {
            respondFailure(event, null, "Das globale Ratelimit wurde erreicht.", EmbedColor.FAILURE);
            return;
        }

        final OptionMapping dateOption = event.getOption("date");
        final LocalDate date = event.checkOption(dateOption, LocalDate.now(), mapping -> DateUtil.toDate(mapping.getAsString()));

        if (date == null) {
            final String input = ObjectUtil.requireNonNullOrElse(dateOption, null, OptionMapping::getAsString);
            respondFailure(event, input, "Invalides Format", EmbedColor.WARNING);
            return;
        }

        final String formattedDate = DateUtil.formatDate(date);

        if (!DateUtil.inMinimumRange(date)) {
            respondFailure(event, formattedDate, "Veraltetes Datum", EmbedColor.WARNING);
            return;
        }

        final boolean weekend = DateUtil.isWeekend(date);

        if (weekend) {
            respondFailure(event, formattedDate, "Wochenende", EmbedColor.WARNING);
            return;
        }

        if (bot.getTimetableManager().isAbsentDate(date)) {
            respondFailure(event, formattedDate, "Freier Tag", EmbedColor.WARNING);
            return;
        }

        final boolean update = event.getOption("update", false, OptionMapping::getAsBoolean);

        event.deferReply().queue(hook -> bot.getTimetableManager()
                .retrieveTimetable(date, update)
                .queue(timetable -> {
                            SchoolUtil.sendTimetable(hook, timetable, user);
                            verificationManager.authorized(true);
                        },
                        throwable -> {

                            if (throwable instanceof ErrorResponseException responseException) {

                                final int code = responseException.getCode();

                                if (code == ErrorResponseException.UNAUTHORIZED) {
                                    verificationManager.authorized(false);
                                }

                                final String response = switch (code) {
                                    case ErrorResponseException.NOT_FOUND -> "Existiert nicht";
                                    case ErrorResponseException.UNAUTHORIZED -> "Nicht autorisiert";
                                    default -> "Fehler: %s".formatted(code);
                                };

                                final MessageEmbed embed = failureEmbed(formattedDate, response, EmbedColor.FAILURE);

                                hook.sendMessageEmbeds(embed).queue();
                                return true;
                            } else {
                                hook.sendMessage("Something bad happened. Please redirect this incidence.").queue();
                            }

                            return false;
                        }
                ));

    }

    private void respondFailure(@NotNull SlashCommandExecuteEvent event,
                                @Nullable String input,
                                @NotNull String response,
                                @NotNull EmbedColor color) {
        event.replyEmbeds(failureEmbed(input, response, color))
                .setEphemeral(true)
                .queue();
    }

    private @NotNull MessageEmbed failureEmbed(@Nullable String input,
                                               @NotNull String response,
                                               @NotNull EmbedColor color) {
        final EmbedBuilder builder = EmbedUtil.withColor(color);

        if (input != null) {
            builder.addField("Datum", MarkdownUtil.monospace(input), false);
        }

        return EmbedUtil.addResponse(builder, MarkdownUtil.codeblock(response))
                .build();
    }

}