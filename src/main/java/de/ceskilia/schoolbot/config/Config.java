package de.ceskilia.schoolbot.config;

import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.concurrent.TimeUnit;

public interface Config<T> {

    long DEFAULT_UPDATE_INTERVAL = 20_000; //20sec

    static @NotNull ArrayConfig arrayConfig(@NotNull String path) {
        return arrayConfig(path, DEFAULT_UPDATE_INTERVAL, null);
    }

    static @NotNull ArrayConfig arrayConfig(@NotNull String path,
                                            long updateInterval,
                                            @Nullable TimeUnit updateUnit) {
        return new ArrayConfig(path, updateInterval, updateUnit);
    }

    static @NotNull DefaultConfig defaultConfig(@NotNull String path) {
        return defaultConfig(path, null);
    }

    static @NotNull DefaultConfig defaultConfig(@NotNull String path, @Nullable DataObject defaultData) {
        return defaultConfig(path, defaultData, DEFAULT_UPDATE_INTERVAL, null);
    }

    static @NotNull DefaultConfig defaultConfig(@NotNull String path,
                                                @Nullable DataObject defaultData,
                                                long updateInterval,
                                                @Nullable TimeUnit updateUnit) {
        return new DefaultConfig(path, defaultData, updateInterval, updateUnit);
    }

    @NotNull File getFile();

    @NotNull T getData();

    default @NotNull T retrieveData() {
        return System.currentTimeMillis() - getLastUpdate() <= getUpdateInterval() ? getData() : readData();
    }

    @NotNull T readData();

    long getUpdateInterval();

    long getLastUpdate();

    void save();

    void createFileIfAbsent();

}
