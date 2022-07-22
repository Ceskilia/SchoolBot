package de.ceskilia.schoolbot.command;

import de.ceskilia.cutils.command.CommandDiscriptor;
import de.ceskilia.cutils.command.Configuration;
import de.ceskilia.cutils.command.slashcommand.SlashCommandConfiguration;
import de.ceskilia.cutils.event.command.GuildSlashCommandExecuteEvent;
import de.ceskilia.cutils.event.command.SlashCommandExecuteEvent;
import de.ceskilia.schoolbot.SchoolBot;
import de.ceskilia.schoolbot.action.ErrorResponseException;
import de.ceskilia.schoolbot.util.embed.EmbedResponseBuilder;
import de.ceskilia.schoolbot.util.embed.EmbedResponseType;
import de.ceskilia.schoolbot.util.lang.DateUtil;
import de.ceskilia.schoolbot.util.lang.SchoolUtil;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MarkdownUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDate;

public class TimetableCommand implements CommandDiscriptor<GuildSlashCommandExecuteEvent> {

    @Override
    public @NotNull Configuration<GuildSlashCommandExecuteEvent> buildConfiguration() {
        return SlashCommandConfiguration.guildOnly("timetable","Zeigt den Vertretungsplan an")
                .option(OptionType.INTEGER,"day","Das Datum des Vertretungsplans")
                .option(OptionType.BOOLEAN,"update","Den angefragten Vertretungsplan aktualisieren")
                .build(this);
    }

    @Override
    public void execute(@NotNull GuildSlashCommandExecuteEvent event) {

        final SchoolBot bot = event.getBot().cast(SchoolBot.class);
        final VerificationManager verificationManager = bot.getVerificationManager();
        final User user = event.getUser();

        if(verificationManager.isBlacklisted(user.getIdLong())) {
            respondFailure(event,null,null,"Du wurdest geschwarzelistet.",EmbedResponseType.FAIL);
            return;
        }

        if(!verificationManager.isVerified(user.getIdLong())) {
            respondFailure(event,null,null,"Bitte verifiziere dich zuerst.",EmbedResponseType.WARN);
            return;
        }

        if(!verificationManager.canRequest()) {
            respondFailure(event,null,null,"Die letzte Anfrage konnte nicht autorisiert werden.",EmbedResponseType.FAIL);
            return;
        }

        final OptionMapping dayOption = event.getOption("day");
        final LocalDate requestDate = event.checkOption(dayOption, LocalDate.now(), mapping -> {
            long option = mapping.getAsLong();
            return DateUtil.isValidDay(option) ? DateUtil.assumeDate((int) option) : null;
        });

        final boolean weekend = DateUtil.isWeekend(requestDate);

        if(requestDate == null || weekend) {
            respondFailure(event,dayOption,weekend ? requestDate : null,"Wird nicht existieren",EmbedResponseType.WARN);
            return;
        }

        if(bot.getTimetableManager().isAbsentDate(requestDate)) {
            respondFailure(event,dayOption, requestDate,"Freier Tag",EmbedResponseType.WARN);
            return;
        }

        final boolean update = event.getOption("update",false, OptionMapping::getAsBoolean);

        event.deferReply().queue(hook -> bot.getTimetableManager()
                .retrieveTimetable(requestDate, update)
                .queue(timetable -> {
                            SchoolUtil.sendTimetable(hook, timetable, user);
                            verificationManager.authorized(true);
                        },
                        throwable -> {

                            if(throwable instanceof ErrorResponseException responseException) {

                                final int code = responseException.getCode();

                                if(code == ErrorResponseException.UNAUTHORIZED) {
                                    verificationManager.authorized(false);
                                }

                                final String response = switch (code) {
                                    case ErrorResponseException.NOT_FOUND -> "Existiert nicht";
                                    case ErrorResponseException.UNAUTHORIZED -> "Nicht autorisiert";
                                    default -> "Fehler: %s".formatted(code);
                                };

                                final MessageEmbed embed = failureEmbed(dayOption, requestDate, response,EmbedResponseType.FAIL);

                                hook.sendMessageEmbeds(embed).queue();
                            } else {
                                hook.sendMessage("Something bad happened. Please redirect this incidence.").queue();
                            }

                            return false;
                        }
                ));

    }

    private void respondFailure(@NotNull SlashCommandExecuteEvent event,
                                @Nullable OptionMapping input,
                                @Nullable LocalDate date,
                                @NotNull String response,
                                @NotNull EmbedResponseType type) {
        event.replyEmbeds(failureEmbed(input, date, response, type))
                .setEphemeral(true)
                .queue();
    }

    private @NotNull MessageEmbed failureEmbed(@Nullable OptionMapping input,
                                                @Nullable LocalDate date,
                                                @NotNull String response,
                                                @NotNull EmbedResponseType type) {
        final EmbedResponseBuilder builder = new EmbedResponseBuilder();

        if(input != null)
            builder.addInput(MarkdownUtil.monospace(input.getAsLong() + "."));
        if(date != null)
            builder.addField("Datum", MarkdownUtil.monospace(DateUtil.formatDate(date)),false);

        return builder.addResponse(MarkdownUtil.codeblock(response), type)
                .build();
    }

}