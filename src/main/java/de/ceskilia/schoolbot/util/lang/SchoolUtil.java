package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.schoolbot.school.timetable.Timetable;
import de.ceskilia.schoolbot.util.SystemInfo;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageAction;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SchoolUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchoolUtil.class);

    private static final String IMAGE_NAME = "timetable.png";
    private static final String DEFAULT_IMAGE_NAME = "failure.png";
    private static final File DEFAULT_IMAGE = new File("pics/" + DEFAULT_IMAGE_NAME);

    static {

        final String parent = DEFAULT_IMAGE.getParent();

        if(parent != null) {
            new File(parent).mkdirs(); // use this to avoid checked exception handling as the path is valid
        }

        if(!DEFAULT_IMAGE.exists()) {
            LOGGER.debug("No default image ({}) for a failed image creation is set. Using default failure action instead.", DEFAULT_IMAGE_NAME);
        }

    }

    private SchoolUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static void sendTimetable(@NotNull InteractionHook hook, @NotNull Timetable timetable, @NotNull User requester) {
        final WebhookMessageAction<Message> action = hook.sendMessageEmbeds(toEmbed(timetable, requester));

        if(!timetable.hasLessons()) {
            action.queue();
            return;
        }

        ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.addFile(inputStream, IMAGE_NAME))
                .exceptionally(throwable -> {
                    if(!DEFAULT_IMAGE.exists())
                        return action.setContent("Ein Fehler ist bei der Bilderstellung aufgetreten.");
                    return action.addFile(DEFAULT_IMAGE, IMAGE_NAME);
                })
                .thenAccept(WebhookMessageAction::queue);
    }

    public static @NotNull CompletableFuture<MessageAction> sendTimetable(@NotNull MessageChannel channel, @NotNull Timetable timetable, @NotNull User requester) {
        return performTimetableAction(channel.sendMessageEmbeds(toEmbed(timetable, requester)), timetable);
    }

    public static @NotNull CompletableFuture<MessageAction> editTimetable(@NotNull MessageChannel channel, long messageId, @NotNull Timetable timetable, @NotNull User requester) {
        return performTimetableAction(channel.editMessageEmbedsById(messageId, toEmbed(timetable, requester)), timetable);
    }

    private static @NotNull CompletableFuture<MessageAction> performTimetableAction(@NotNull MessageAction action, @NotNull Timetable timetable) {
        if(!timetable.hasLessons())
            return CompletableFuture.completedFuture(action);
        return ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.addFile(inputStream, IMAGE_NAME))
                .exceptionally(throwable -> {
                    if(!DEFAULT_IMAGE.exists())
                        return action.content("Ein Fehler ist bei der Bilderstellung aufgetreten.");
                    return action.addFile(DEFAULT_IMAGE, IMAGE_NAME);
                });
    }

    private static @NotNull MessageEmbed toEmbed(@NotNull Timetable timetable, @NotNull User requester) {

        final EmbedBuilder builder = EmbedColor.INFORMATION.withEmbedBuilder()
                .setTitle(timetable.getDate())
                .setDescription(TimeFormat.DATE_TIME_LONG.format(timetable.getLastChange().toInstant(ZoneOffset.UTC)))
                .setFooter("letztes Update", requester.getAvatarUrl())
                .setTimestamp(Instant.ofEpochMilli(timetable.getCreationTime()));
        final List<String> absentClasses = timetable.getAbsentClasses();
        final List<String> changedClasses = timetable.getChangedClasses();
        final List<String> extraInformation = timetable.getExtraInformation();

        if(!absentClasses.isEmpty())
            builder.addField("Abwesende Klassen",compromiseData(absentClasses),false);
        if(!changedClasses.isEmpty())
            builder.addField("Klassen mit Änderung",compromiseData(changedClasses),false);
        if(!extraInformation.isEmpty())
            builder.addField("Zusätzliche Informationen",String.join("\n",extraInformation),false);
        if(timetable.hasLessons())
            builder.setImage("attachment://" + IMAGE_NAME);
        return builder.build();
    }

    private static @NotNull String compromiseData(@NotNull List<String> data) {
        return IntStream.range(7, 13).mapToObj(String::valueOf).map(grade -> { // go through 7-12 (all classes)
            if(data.stream().filter(c -> c.startsWith(grade)).count() > 3) // if more than 3 values are present, just add the class itself
                return grade;
            return data.stream().filter(c -> c.startsWith(grade)).collect(Collectors.joining(", ")); // join up to 3 classes
        }).filter(c -> !c.isBlank()).collect(Collectors.joining(", "));
    }

    public static @NotNull List<String> fetchExtraInformation(@Nullable DataObject data) {

        if(data == null || !data.hasKey("fuss")) {
            return Collections.emptyList();
        }

        final DataArray footer = data.getObject("fuss").getArray("fusszeile");

        return footer.stream(DataArray::getObject)
                .map(info -> info.getString("fussinfo"))
                .filter(text -> !text.isBlank())
                .filter(text -> !text.startsWith("Achtung!")) // this is usually the heading which should be omitted
                .toList();
    }

    public static @NotNull List<LocalDate> fetchAbsentDates(@NotNull DataObject data) {

        if(!data.hasKey("freietage")) {
            return Collections.emptyList();
        }

        final DataObject absentDays = data.getObject("freietage");

        return absentDays.getArray("ft").stream(DataArray::getString)
                .map(date -> LocalDate.parse(SystemInfo.currentCentury() + date, DateTimeFormatter.BASIC_ISO_DATE))
                .collect(Collectors.toCollection(LinkedList::new));
    }

}
