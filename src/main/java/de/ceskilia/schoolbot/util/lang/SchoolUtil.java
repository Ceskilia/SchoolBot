package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.schoolbot.school.timetable.Timetable;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.requests.restaction.MessageEditAction;
import net.dv8tion.jda.api.requests.restaction.WebhookMessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.dv8tion.jda.api.utils.messages.MessageCreateRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class SchoolUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchoolUtil.class);

    private static final String IMAGE_NAME = "timetable.png";
    private static final String DEFAULT_IMAGE_NAME = "failure.png";
    private static final String FAILED_IMAGE_CREATION = "Ein Fehler ist bei der Bilderstellung aufgetreten.";
    private static final File DEFAULT_IMAGE = new File("pics/" + DEFAULT_IMAGE_NAME);
    private static final FileUpload DEFAULT_IMAGE_UPLOAD = FileUpload.fromData(DEFAULT_IMAGE, IMAGE_NAME);

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
        sendTimetable(hook.sendMessageEmbeds(toEmbed(timetable, requester)), timetable)
                .thenAccept(WebhookMessageCreateAction::queue);
    }

    public static @NotNull CompletableFuture<MessageCreateAction> sendTimetable(@NotNull MessageChannel channel, @NotNull Timetable timetable, @NotNull User requester) {
        return sendTimetable(channel.sendMessageEmbeds(toEmbed(timetable, requester)), timetable);
    }

    private static <T extends MessageCreateRequest<T>> @NotNull CompletableFuture<T> sendTimetable(@NotNull T action, @NotNull Timetable timetable) {
        if(!timetable.hasLessons())
            return CompletableFuture.completedFuture(action);
        return ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.addFiles(FileUpload.fromData(inputStream, IMAGE_NAME)))
                .exceptionally(throwable -> {
                    if(!DEFAULT_IMAGE.exists())
                        return action.setContent(FAILED_IMAGE_CREATION);
                    return action.addFiles(DEFAULT_IMAGE_UPLOAD);
                });
    }

    public static @NotNull CompletableFuture<MessageEditAction> editTimetable(@NotNull MessageChannel channel, long messageId, @NotNull Timetable timetable, @NotNull User requester) {
        final MessageEditAction action = channel.editMessageEmbedsById(messageId, toEmbed(timetable, requester));

        if(!timetable.hasLessons())
            return CompletableFuture.completedFuture(action);
        return ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.setFiles(FileUpload.fromData(inputStream, IMAGE_NAME)))
                .exceptionally(throwable -> {
                    if (!DEFAULT_IMAGE.exists())
                        return action.setContent(FAILED_IMAGE_CREATION);
                    return action.setFiles(DEFAULT_IMAGE_UPLOAD);
                });
    }

    public static @NotNull MessageEmbed toEmbed(@NotNull Timetable timetable, @NotNull User requester) {

        final EmbedBuilder builder = EmbedUtil.withColor(timetable.isOld() ? EmbedColor.DEPRECATED : EmbedColor.INFORMATION)
                .setTitle(timetable.getDate())
                .setDescription(TimeFormat.DATE_TIME_LONG.format(timetable.getLastChange().toInstant(ZoneOffset.UTC)))
                .setFooter("letztes Update", requester.getEffectiveAvatarUrl())
                .setTimestamp(Instant.ofEpochMilli(timetable.getCreationTime()));
        final List<String> absentClasses = timetable.getAbsentClasses();
        final List<String> changedClasses = timetable.getChangedClasses();
        final List<String> extraInformation = timetable.getExtraInformation();

        if(!absentClasses.isEmpty())
            builder.addField("Abwesende Klassen", compromiseClassData(absentClasses),false);
        if(!changedClasses.isEmpty())
            builder.addField("Klassen mit Änderung", compromiseClassData(changedClasses),false);
        if(!extraInformation.isEmpty())
            builder.addField("Zusätzliche Informationen",formatExtraInformation(extraInformation),false);
        if(timetable.hasLessons())
            builder.setImage("attachment://" + IMAGE_NAME);
        return builder.build();
    }

    public static @NotNull List<String> fetchExtraInformation(@Nullable DataObject data) {

        if(data == null || !data.hasKey("fuss")) {
            return Collections.emptyList();
        }

        final DataArray footer = JsonUtil.safeToArray(data.getObject("fuss"), "fusszeile");

        return footer.stream(DataArray::getObject)
                .map(info -> info.getString("fussinfo"))
                .filter(text -> !text.isBlank())
                .filter(text -> !text.startsWith("Achtung!")) // this is usually the heading which may be omitted
                .toList();
    }

    private static @NotNull String formatExtraInformation(@NotNull List<String> information) {
        return information.stream()
                .map(SchoolUtil::lineInformation)
                .collect(Collectors.joining("\n"));
    }

    private static @NotNull String lineInformation(@NotNull String entry) {

        final StringBuilder result = new StringBuilder();
        int currentIndex;

        for(int i = 0; i < entry.length(); i++) {

            char currentSymbol = entry.charAt(i);
            result.append(currentSymbol);

            if(nextLineBreak(i)) {

                // starting at the next index
                for(currentIndex = i + 1; currentIndex < entry.length(); currentIndex++) {
                    currentSymbol = entry.charAt(currentIndex);

                    // if the character is a whitespace -> add linebreak
                    if(Character.isWhitespace(currentSymbol)) {
                        break;
                    }

                    result.append(currentSymbol);
                }

                result.append("\n");
                i = currentIndex;
            }

        }

        return result.toString();
    }

    private static boolean nextLineBreak(int index) {
        // new linebreak every 40 chars (index starts at 0)
        return index != 0 && index % 39 == 0;
    }

    private static @NotNull String compromiseClassData(@NotNull List<String> data) {
        return IntStream.range(7, 13).mapToObj(String::valueOf).map(grade -> { // go through all classes (7-12)
            if(data.stream().filter(c -> c.startsWith(grade)).count() > 3) // if more than 3 values are present, just add the class itself
                return grade;
            return data.stream().filter(c -> c.startsWith(grade)).collect(Collectors.joining(", ")); // join up to 3 classes
        }).filter(c -> !c.isBlank()).collect(Collectors.joining(", "));
    }

}
