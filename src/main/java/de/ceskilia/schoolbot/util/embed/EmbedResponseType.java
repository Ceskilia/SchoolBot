package de.ceskilia.schoolbot.util.embed;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public enum EmbedResponseType implements Comparable<EmbedResponseType> {

    SUCCESS(EmbedColor.SUCCESS.getColor()),
    WARN(EmbedColor.WARNING.getColor()),
    FAIL(EmbedColor.FAILURE.getColor());

    private final Color color;

    EmbedResponseType(@NotNull Color color) {
        this.color = color;
    }

    public @NotNull Color getColor() {
        return color;
    }

    public boolean isLowerThan(@Nullable EmbedResponseType other) {
        return other != null && ordinal() > other.ordinal();
    }

}