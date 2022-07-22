package de.ceskilia.schoolbot.util.embed;

import de.ceskilia.schoolbot.util.Emote;
import de.ceskilia.schoolbot.util.lang.ImageUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public enum EmbedColor {

    DEFAULT(34,34,34),
    INFORMATION(41,161,241),
    SUCCESS(18,169,9, Emote.GREEN_CHECK),
    WARNING(253, 242, 22, Emote.WARNING),
    FAILURE(234,56,56, Emote.NO_ENTRY);

    private final Color color;
    private final Emote emote;

    EmbedColor(int r, int g, int b) {
        this(r, g, b,null);
    }

    EmbedColor(int r, int g, int b, @Nullable Emote emote) {
        this.color = new Color(
                ImageUtil.validateColorValue(r,"Red"),
                ImageUtil.validateColorValue(g,"Green"),
                ImageUtil.validateColorValue(b,"Blue")
        );
        this.emote = emote;
    }

    public @NotNull Color getColor() {
        return color;
    }

    public @Nullable Emote getDefaultEmote() {
        return emote;
    }

    public @NotNull EmbedBuilder withEmbedBuilder() {
        return new EmbedBuilder().setColor(getColor());
    }

}
