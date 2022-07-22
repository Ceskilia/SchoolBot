package de.ceskilia.schoolbot.config;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public interface Config<T> {

    long DEFAULT_UPDATE_INTERVAL = 20_000;

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
