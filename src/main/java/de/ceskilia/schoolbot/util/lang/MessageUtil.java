package de.ceskilia.schoolbot.util.lang;

import de.ceskilia.config.DefaultConfig;
import de.ceskilia.cutils.DiscordBot;
import de.ceskilia.schoolbot.util.Emote;
import de.ceskilia.schoolbot.util.embed.EmbedColor;
import de.ceskilia.schoolbot.util.embed.EmbedUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.requests.RestAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class MessageUtil {

    private MessageUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static void notifyAuthor(@NotNull DiscordBot bot, @NotNull String message) {
        RestAction.allOf(Arrays.stream(bot.getAuthors())
                .map(author -> author.toUser(bot.getJDA()))
                .map(restAction -> restAction.flatMap(user ->
                        user.openPrivateChannel().flatMap(channel -> channel.sendMessage(message)))
                )
                .toList()
        ).queue();
    }

    public static void notifyAuthor(@NotNull DiscordBot bot, @NotNull MessageEmbed embed) {
        RestAction.allOf(Arrays.stream(bot.getAuthors())
                .map(author -> author.toUser(bot.getJDA()))
                .map(restAction -> restAction.flatMap(user ->
                        user.openPrivateChannel().flatMap(channel -> channel.sendMessageEmbeds(embed)))
                )
                .toList()
        ).queue();
    }

    public static @NotNull Emote check(boolean expression) {
        return expression ? Emote.GREEN_CHECK : Emote.RED_CROSS;
    }

    public static @NotNull Emote isSet(@NotNull DefaultConfig config, @NotNull String key) {
        return check(config.isValueSet(key));
    }

    public static @NotNull Emote isNotBlank(@NotNull DefaultConfig config, @NotNull String key) {
        return check(config.getData().hasKey(key));
    }

    public static boolean canSendMessage(@Nullable GuildChannel channel) {

        if (channel == null) {
            return false;
        }

        final Member selfMember = channel.getGuild().getSelfMember();

        return selfMember.hasAccess(channel) && selfMember.hasPermission(channel, Permission.MESSAGE_SEND);
    }

    public static @NotNull MessageEmbed embed(@NotNull EmbedColor color, @NotNull String title, @NotNull String description) {
        return EmbedUtil.withColor(color)
                .setTitle((color.getDefaultEmote() != null) ? color.getDefaultEmote().append(title) : title)
                .setDescription(description)
                .build();
    }

}
