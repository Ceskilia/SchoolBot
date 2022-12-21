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

    /**
     * The maximum size of an information line.
     */
    private static final int MAX_INFORMATION_LENGTH = 40;

    /**
     * The name of the timetable image attachment is sent with.
     */
    private static final String IMAGE_NAME = "timetable.png";
    /**
     * The name of the image, when a timetable image could not be created.
     */
    private static final String FAILED_CREATION_IMAGE_NAME = "failure.png";
    /**
     * The message when a timetable could not be created and no image for failure has been set.
     */
    private static final String FAILED_IMAGE_CREATION = "Ein Fehler ist bei der Bilderstellung aufgetreten.";
    /**
     * The image representing that a timetable image creation failed.
     */
    private static final File FAILED_CREATION_IMAGE = new File("pics/" + FAILED_CREATION_IMAGE_NAME);
    /**
     * The {@link FileUpload} that combines the {@link #FAILED_CREATION_IMAGE} with the {@link #IMAGE_NAME} so the image
     * can be added and uploaded as the attachment to the sent message.
     */
    private static final FileUpload DEFAULT_IMAGE_UPLOAD = FileUpload.fromData(FAILED_CREATION_IMAGE, IMAGE_NAME);

    static {

        final String parent = FAILED_CREATION_IMAGE.getParent();

        if(parent != null) {
            new File(parent).mkdirs(); // use this to avoid checked exception handling as the path is valid
        }

        if(!FAILED_CREATION_IMAGE.exists()) {
            LOGGER.debug("No default image ({}) for a failed image creation is set. Using default failure action instead.", FAILED_CREATION_IMAGE_NAME);
        }

    }

    private SchoolUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    /**
     * Sends the {@link #toEmbed(Timetable, User) timetable embed} with the user to the provided {@link InteractionHook}.
     *
     * <br> If an exception occurs, the {@link #FAILED_CREATION_IMAGE} or the {@link #FAILED_IMAGE_CREATION} is added instead.
     *
     * @param hook the webhook to send the timetable embed to
     * @param timetable the timetable to create the embed from
     * @param requester the user that requests the timetable
     */
    public static void sendTimetable(@NotNull InteractionHook hook, @NotNull Timetable timetable, @NotNull User requester) {
        addTimetableImage(hook.sendMessageEmbeds(toEmbed(timetable, requester)), timetable)
                .thenAccept(WebhookMessageCreateAction::queue);
    }

    /**
     * Returns a completable future with a modified request action.
     * Moreover, this prepares the sending of the {@link #toEmbed(Timetable, User) timetable embed} with the user to the
     * provided {@link MessageChannel}.
     *
     * <br> The action actually needs to be sent to discord!
     * <br> If an exception occurs, the {@link #FAILED_CREATION_IMAGE} or the {@link #FAILED_IMAGE_CREATION} is added instead.
     *
     * @param channel the channel to send the timetable embed to
     * @param timetable the timetable to create the embed from
     * @param requester the user that requests the timetable
     * @return a completable future with a modified request
     */
    public static @NotNull CompletableFuture<MessageCreateAction> sendTimetable(@NotNull MessageChannel channel, @NotNull Timetable timetable, @NotNull User requester) {
        return addTimetableImage(channel.sendMessageEmbeds(toEmbed(timetable, requester)), timetable);
    }

    /**
     * Returns a completable future with a modified request action.
     * Moreover, this adds the {@link Timetable#getImage() timetable image} to the provided {@link MessageCreateRequest}.
     *
     * <br> The action actually needs to be sent to discord!
     * <br> If an exception occurs, the {@link #FAILED_CREATION_IMAGE} or the {@link #FAILED_IMAGE_CREATION} is added instead.
     *
     * @param action the request action to add the image too
     * @param timetable the timetable to get the image from
     * @param <T> the message request type
     * @return a completable future with a modified request
     */
    private static <T extends MessageCreateRequest<T>> @NotNull CompletableFuture<T> addTimetableImage(@NotNull T action, @NotNull Timetable timetable) {
        if(!timetable.hasLessons())
            return CompletableFuture.completedFuture(action);
        return ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.addFiles(FileUpload.fromData(inputStream, IMAGE_NAME)))
                .exceptionally(throwable -> {
                    if(!FAILED_CREATION_IMAGE.exists())
                        return action.setContent(FAILED_IMAGE_CREATION);
                    return action.addFiles(DEFAULT_IMAGE_UPLOAD);
                });
    }

    /**
     * Returns a completable future with a modified edit action.
     * Moreover, this prepares the editing of the given message in the provided {@link MessageChannel} with a new
     * {@link #toEmbed(Timetable, User) timetable embed}.
     *
     * <br> The action actually needs to be sent to discord!
     * <br> If an exception occurs, the {@link #FAILED_CREATION_IMAGE} or the {@link #FAILED_IMAGE_CREATION} is added instead.
     *
     * @param channel the channel to send the timetable embed to
     * @param messageId the message id of the message to edit
     * @param timetable the timetable to create the embed from
     * @param requester the user that requests the timetable
     * @return a completable future with a modified request
     */
    public static @NotNull CompletableFuture<MessageEditAction> editTimetable(@NotNull MessageChannel channel, long messageId, @NotNull Timetable timetable, @NotNull User requester) {
        final MessageEditAction action = channel.editMessageEmbedsById(messageId, toEmbed(timetable, requester));

        if(!timetable.hasLessons())
            return CompletableFuture.completedFuture(action);
        return ImageUtil.createImageInput(timetable)
                .thenApply(inputStream -> action.setFiles(FileUpload.fromData(inputStream, IMAGE_NAME)))
                .exceptionally(throwable -> {
                    if (!FAILED_CREATION_IMAGE.exists())
                        return action.setContent(FAILED_IMAGE_CREATION);
                    return action.setFiles(DEFAULT_IMAGE_UPLOAD);
                });
    }

    /**
     * Returns a new {@link MessageEmbed} containing the content of the provided {@link Timetable}.
     * The provided user is marked in the {@link MessageEmbed#getFooter() footer} as the requester.
     *
     * @param timetable the timetable to create the embed from
     * @param requester the user that requests the timetable
     * @return a message embed of the timetable
     */
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

    /**
     * Returns a string with compromised content of the provided classes.
     * If there are more than 3 classes that start the with the same level, only the level is represented.
     *
     * <br> Example: 10A, 10B, 10C, 10D -> 10
     *
     * @param classes the classes to compromise
     * @return a string with the compromised data
     */
    private static @NotNull String compromiseClassData(@NotNull List<String> classes) {
        return IntStream.range(7, 13).mapToObj(String::valueOf).map(level -> { // go through all classes (7-12)
            if(classes.stream().filter(c -> c.startsWith(level)).count() > 3) // if more than 3 values are present, just add the class itself
                return level;
            return classes.stream().filter(c -> c.startsWith(level)).collect(Collectors.joining(", ")); // join up to 3 classes
        }).filter(c -> !c.isBlank()).collect(Collectors.joining(", "));
    }

    /**
     * Returns a list with the extra information of the {@link Timetable}.
     *
     * <br> Blank headings and the heading "Achtung!" are excluded!
     *
     * @see Timetable#getExtraInformation()
     *
     * @param data the data to fetch the information from
     * @return a list with the extra information
     */
    public static @NotNull List<String> fetchExtraInformation(@Nullable DataObject data) {

        if(data == null || !data.hasKey("fuss")) {
            return Collections.emptyList();
        }

        final DataArray footer = JsonUtil.safeToArray(data.getObject("fuss"), "fusszeile");

        return footer.stream(DataArray::getObject)
                .map(info -> info.getString("fussinfo"))
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .filter(text -> !text.startsWith("Achtung!")) // this is usually the heading which may be omitted
                .toList();
    }

    /**
     * Returns a combined, formatted string with the {@link #lineInformation(String) lined information} separated with a linebreak.
     *
     * @param information the information to line and combine
     * @return a combined, formatted, lined string
     */
    private static @NotNull String formatExtraInformation(@NotNull List<String> information) {
        return information.stream()
                .map(SchoolUtil::lineInformation)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Returns a string with a new linebreak every {@link #MAX_INFORMATION_LENGTH}. This break is only added, when the
     * character at that index is a whitespace. Else the break is delayed to the point, a whitespace first occurs.
     *
     * @param entry the string/entry to format
     * @return a formatted, lined entry
     */
    private static @NotNull String lineInformation(@NotNull String entry) {

        final StringBuilder result = new StringBuilder();

        for(int i = 0; i < entry.length(); i++) {

            char currentSymbol = entry.charAt(i);
            result.append(currentSymbol);

            if(nextLineBreak(i)) {

                for(++i; i < entry.length(); i++) {
                    currentSymbol = entry.charAt(i);

                    // if the character is a whitespace -> add linebreak
                    if(Character.isWhitespace(currentSymbol)) {
                        break;
                    }

                    result.append(currentSymbol);
                }

                result.append("\n");
            }

        }

        return result.toString();
    }

    private static boolean nextLineBreak(int index) {
        // new linebreak every 40 chars (index starts at 0)
        return index != 0 && index % (MAX_INFORMATION_LENGTH - 1) == 0;
    }

}
