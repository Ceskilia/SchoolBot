package de.ceskilia.schoolbot;

import de.ceskilia.config.Config;
import de.ceskilia.config.DefaultConfig;
import de.ceskilia.config.data.ConfigDataObject;
import de.ceskilia.cutils.util.internal.Action;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.exceptions.InvalidTokenException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(@Nullable String[] args) {
        final Dotenv dotenv = Dotenv.load();
        final DefaultConfig config = Config.defaultConfig("config/settings.json", ConfigDataObject.empty()
                .put("token", "<BOT-TOKEN HERE>")
                .put("timetableURL", dotenv.get("TIMETABLE_URL"))
        );

        config.verifyValue("token", () -> "Please provide a token:");
        Action.create(() -> new SchoolBot(config).load())
                .onSuccess(bot -> LOGGER.info("Successfully built {} @ {}", bot.getName(), new Date()))
                .onFailure(throwable -> {
                    LOGGER.error("An exception occurred. Could not launch the application.", throwable);

                    if (throwable instanceof InvalidTokenException) {
                        config.restoreDefault();
                    }

                    System.exit(1);
                })
                .perform();
    }

}
