package de.ceskilia.schoolbot;

import de.ceskilia.config.DefaultConfig;
import de.ceskilia.cutils.DiscordBot;
import de.ceskilia.cutils.essential.Author;
import de.ceskilia.cutils.util.CUtilsInfo;
import de.ceskilia.schoolbot.changelog.Changelog;
import de.ceskilia.schoolbot.changelog.Feature;
import de.ceskilia.schoolbot.command.*;
import de.ceskilia.schoolbot.school.channel.BroadcastChannelManager;
import de.ceskilia.schoolbot.school.timetable.TimetableManager;
import de.ceskilia.schoolbot.school.timetable.TimetableManagerImpl;
import de.ceskilia.schoolbot.school.verification.VerificationManager;
import de.ceskilia.schoolbot.util.SystemInfo;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.List;

public class SchoolBot extends DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchoolBot.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final DefaultConfig config;
    private final Changelog changelog;
    private final VerificationManager verificationManager;
    private final BroadcastChannelManager channelManager;
    private final TimetableManager timetableManager;
    private final OkHttpClient httpClient;

    protected SchoolBot(@NotNull DefaultConfig config) throws LoginException {
        super(config.getData().getString("token"),
                "Schoolbot",
                "1.0-ALPHA",
                Author.fromId(363332011454103555L)
        );

        this.config = config;
        this.changelog = new Changelog(features());
        this.verificationManager = new VerificationManager();
        this.channelManager = new BroadcastChannelManager(this);
        this.timetableManager = new TimetableManagerImpl(this);
        this.httpClient = new OkHttpClient.Builder().authenticator((unused, response) -> {

            final Request request = response.request();

            if (request.header(AUTHORIZATION_HEADER) != null) {
                return null;
            }

            return request.newBuilder()
                    .header(AUTHORIZATION_HEADER, verificationManager.getCredentials())
                    .build();
        }).build();
    }

    @Override
    protected void onLoad() {
        configureBuilderOptions(builder -> builder
                .setActivity(Activity.playing("starte..."))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
        );
    }

    @Override
    protected void onReady() {

        addCommands(
                new PingCommand(),
                new ChangelogCommand(),
                new VerifyCommand(),
                new TimetableCommand(),
                new OwnerCommand(),
                new SetupCommand()
        );

        updateSlashCommands();
        setStatus(OnlineStatus.ONLINE);
        setActivity(Activity.watching("den Vertretungsplan an."));
        getVerificationManager().getConfig().verifyValue("username");
        getVerificationManager().getConfig().verifyValue("password");
        logStartInformation();
    }

    @Override
    protected void onDisable() {
        getConfig().save();
        getVerificationManager().getConfig().save();
    }

    public @NotNull DefaultConfig getConfig() {
        return config;
    }

    public @NotNull Changelog getChangelog() {
        return changelog;
    }

    public @NotNull VerificationManager getVerificationManager() {
        return verificationManager;
    }

    public @NotNull BroadcastChannelManager getChannelManager() {
        return channelManager;
    }

    public @NotNull TimetableManager getTimetableManager() {
        return timetableManager;
    }

    public @NotNull OkHttpClient getHttpClient() {
        return httpClient;
    }

    private @NotNull List<Feature> features() {
        return Arrays.asList(
                new Feature("Add changelog command", Feature.Type.ADDED),
                new Feature("Update permission system", Feature.Type.UPDATED)
        );
    }

    private void logStartInformation() {
        LOGGER.info("Account:               " + getJDA().getSelfUser());
        LOGGER.info("Java Version:          " + SystemInfo.getJavaVersion());
        LOGGER.info("JDA Version:           " + JDAInfo.VERSION);
        LOGGER.info("JDA-CUtils Version:    " + CUtilsInfo.VERSION);
        LOGGER.info(getName() + " Version:     " + getVersion());
        LOGGER.info("Operating System:      " + SystemInfo.getOperatingSystemName());
        LOGGER.info("Java Vendor:           " + SystemInfo.getVendorName());
        LOGGER.info("Java Home:             " + SystemInfo.getJavaHome());
        LOGGER.info("JDA-CUtils Info:       " + CUtilsInfo.VERSION_DESCRIPTION);
        LOGGER.info("Startup Time:          " + (System.currentTimeMillis() - getStartTime()) + " ms");
    }

}
