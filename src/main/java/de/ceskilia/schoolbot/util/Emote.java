package de.ceskilia.schoolbot.util;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum Emote {

    WARNING("\u26A0"),
    NO_ENTRY("\uD83D\uDEAB"),
    RED_CROSS("\u274C"),
    GREEN_CHECK("\u2705"),
    INFORMATION("\u2139"),
    TABLE_TENNIS_PADDLE("\uD83C\uDFD3"),
    OPEN_FOLDER("\uD83D\uDCC2"),
    MEGA_PHONE("\uD83D\uDCE3"),
    ENVELOPE_ARROW("\uD83D\uDCE9"),
    STOP_WATCH("\u23F1");

    private final String unicode;

    Emote(@NotNull String unicode) {
        this.unicode = unicode;
    }

    public @NotNull String getUnicode() {
        return unicode;
    }

    public @NotNull String append(@Nullable String text) {
        return getUnicode() + (text != null ? " " + text : "");
    }

    public @NotNull Emoji asEmoji() {
        return Emoji.fromUnicode(this.unicode);
    }

}
