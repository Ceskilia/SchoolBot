package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.cutils.utils.util.NumberUtil;
import de.ceskilia.schoolbot.school.timetable.Timetable;
import de.ceskilia.schoolbot.school.timetable.util.TableCreatorKt;
import net.dv8tion.jda.internal.utils.Checks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class ImageUtil {

    private ImageUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static int validateColorValue(int colorValue, @NotNull String name) {
        if (!NumberUtil.inRange(colorValue, 0, 255))
            throw new IllegalArgumentException(String.format("%s may be greater than 0 and lower than 255.", name));
        return colorValue;
    }

    public static boolean isBrighterColor(@NotNull Color first, @Nullable Color second) {
        return second == null || first.getRGB() < second.getRGB();
    }

    public static @NotNull CompletableFuture<InputStream> createImageInput(@NotNull Timetable timetable) {
        Checks.check(timetable.hasLessons(), "Cannot create an image input stream with no lessons.");
        return CompletableFuture.supplyAsync(() -> {
            try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                ImageIO.write(timetable.getImage(), "png", outputStream);

                try (final InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray())) {
                    return inputStream;
                }

            } catch (final IOException e) {
                throw new CompletionException(e);
            }
        });
    }

    // a method designed for legacy decay
    public static @NotNull BufferedImage createTimetableImage(@NotNull List<Timetable.Lesson> lessons) {
        Checks.notEmpty(lessons, "Lessons");

        final String[] text = TableCreatorKt.createTable(lessons).split("\n");
        final BufferedImage image = new BufferedImage(text[0].length() * 17 - 14, text.length * 18 - 15, BufferedImage.TYPE_INT_ARGB);

        final Graphics2D graphics = image.createGraphics();
        graphics.setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 28));
        fillArea(graphics, image.getWidth(), image.getHeight(), Color.WHITE);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < text.length; i++) {
            graphics.drawString(text[i], -7, 18 * i + 10); // random magic numbers through testing
        }

        graphics.dispose();
        return image;
    }

    public static void fillArea(@NotNull Graphics2D graphics, int width, int height, @NotNull Color color) {
        graphics.setColor(color);
        graphics.fillRect(0, 0, width, height);
    }

}