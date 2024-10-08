package de.ceskilia.schoolbot.util.embed;

import de.ceskilia.schoolbot.util.lang.ImageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public final class EmbedUtil {

    private EmbedUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static @NotNull EmbedBuilder withColor(@NotNull EmbedColor color) {
        return new EmbedBuilder().setColor(color.getColor());
    }

    public static @NotNull EmbedBuilder combineFields(@NotNull MessageEmbed... embeds) {
        final EmbedBuilder builder = new EmbedBuilder();
        Color color = null;

        for (final MessageEmbed embed : embeds) {

            for (final MessageEmbed.Field field : embed.getFields()) {
                builder.addField(field);
            }

            final Color embedColor = embed.getColor();

            if (color != null && ImageUtil.isBrighterColor(color, embedColor))
                continue;
            color = embedColor;
        }

        if (color != null)
            builder.setColor(color);
        return builder;
    }

    public static @NotNull EmbedBuilder addInput(@NotNull EmbedBuilder builder, @NotNull String message) {
        return addInput(builder, message, false);
    }

    public static @NotNull EmbedBuilder addInput(@NotNull EmbedBuilder builder, @NotNull String message, boolean inline) {
        return builder.addField("Eingabe", message, inline);
    }

    public static @NotNull EmbedBuilder addResponse(@NotNull EmbedBuilder builder, @NotNull String message) {
        return addResponse(builder, message, false);
    }

    public static @NotNull EmbedBuilder addResponse(@NotNull EmbedBuilder builder, @NotNull String message, boolean inline) {
        return builder.addField("Antwort", message, inline);
    }

    public static @NotNull EmbedBuilder addResponse(@NotNull EmbedBuilder builder, @NotNull String message, @NotNull EmbedColor color) {
        return addResponse(builder, message, color, false);
    }

    public static @NotNull EmbedBuilder addResponse(@NotNull EmbedBuilder builder, @NotNull String message, @NotNull EmbedColor color, boolean inline) {
        return addResponse(builder, message, inline).setColor(color.getColor());
    }

}
