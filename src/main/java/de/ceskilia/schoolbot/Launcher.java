package de.ceskilia.schoolbot;

import de.ceskilia.cutils.utils.lang.Action;
import de.ceskilia.schoolbot.config.DefaultConfig;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Launcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(Launcher.class);

    public static void main(@Nullable String[] args) {

        final DefaultConfig config = new DefaultConfig("config/settings.json", DataObject.empty()
                .put("token", "<BOT-TOKEN HERE>")
                .put("timetableURL", "https://ffg-dbr.de/plaene/vertretungsplan/vplan%s.xml")
        );

        config.requestIfNotSet("token");
        Action.create(() -> new SchoolBot(config))
                .onSuccess(bot -> LOGGER.info("Successfully built {} @ {}", bot.getName(), new Date()))
                .onFailure(throwable -> {
                    LOGGER.error("An exception occurred. Could not launch the application.", throwable);
                    System.exit(1);
                })
                .perform();
    }

}
