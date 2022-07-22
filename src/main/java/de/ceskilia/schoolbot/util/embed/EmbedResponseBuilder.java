package de.ceskilia.schoolbot.util.embed;

import de.ceskilia.schoolbot.util.lang.ImageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.time.temporal.TemporalAccessor;

public class EmbedResponseBuilder extends EmbedBuilder {

    public static @NotNull EmbedResponseBuilder combineFields(@NotNull MessageEmbed... embeds) {

        final EmbedResponseBuilder builder = new EmbedResponseBuilder();
        Color color = null;

        for(final MessageEmbed embed : embeds) {

            for(final MessageEmbed.Field field : embed.getFields()) {
                builder.addField(field);
            }

            final Color embedColor = embed.getColor();

            if(color != null && ImageUtil.isBrighterColor(color, embedColor))
                continue;
            color = embedColor;
        }

        if(color != null)
            builder.setColor(color);
        return builder;
    }

    public @NotNull EmbedResponseBuilder addInput(@NotNull String input) {
        return addInput(input,false);
    }

    public @NotNull EmbedResponseBuilder addInput(@NotNull String input, boolean inline) {
        return addField("Eingabe", input ,inline);
    }


    public @NotNull EmbedResponseBuilder addResponse(@NotNull String response, @NotNull EmbedResponseType type) {
        return addResponse(response, type,false);
    }

    public @NotNull EmbedResponseBuilder addResponse(@NotNull String response, @NotNull EmbedResponseType type, boolean inline) {
        return addField("Antwort", response, inline)
                .setColor(type.getColor());
    }

    @Override
    public @NotNull EmbedResponseBuilder clear() {
        super.clear();
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setTitle(@Nullable String title) {
        super.setTitle(title);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setTitle(@Nullable String title, @Nullable String url) {
        super.setTitle(title, url);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder appendDescription(@NotNull CharSequence description) {
        super.appendDescription(description);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setTimestamp(@Nullable TemporalAccessor temporal) {
        super.setTimestamp(temporal);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setColor(@Nullable Color color) {
        super.setColor(color);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setColor(int color) {
        super.setColor(color);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setThumbnail(@Nullable String url) {
        super.setThumbnail(url);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setImage(@Nullable String url) {
        super.setImage(url);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setAuthor(@Nullable String name) {
        super.setAuthor(name);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setAuthor(@Nullable String name, @Nullable String url) {
        super.setAuthor(name, url);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setAuthor(@Nullable String name, @Nullable String url, @Nullable String iconUrl) {
        super.setAuthor(name, url, iconUrl);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setFooter(@Nullable String text) {
        super.setFooter(text);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder setFooter(@Nullable String text, @Nullable String iconUrl) {
        super.setFooter(text, iconUrl);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder addField(@Nullable MessageEmbed.Field field) {
        super.addField(field);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder addField(@Nullable String name, @Nullable String value, boolean inline) {
        super.addField(name, value, inline);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder addBlankField(boolean inline) {
        super.addBlankField(inline);
        return this;
    }

    @Override
    public @NotNull EmbedResponseBuilder clearFields() {
        super.clearFields();
        return this;
    }

}