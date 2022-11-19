package de.ceskilia.schoolbot.config;

import de.ceskilia.schoolbot.util.lang.JsonUtil;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class DefaultConfig implements Config<DataObject> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConfig.class);
    private static final Scanner SCANNER = new Scanner(System.in);

    private final File file;
    private final DataObject defaultData;
    private final long updateInterval;

    private DataObject data;
    private long lastUpdate;

    public DefaultConfig(@NotNull String path) {
        this(path, null);
    }

    public DefaultConfig(@NotNull String path, @Nullable DataObject defaultData) {
        this(path, defaultData, DEFAULT_UPDATE_INTERVAL, null);
    }

    public DefaultConfig(@NotNull String path,
                         @Nullable DataObject defaultData,
                         long updateInterval,
                         @Nullable TimeUnit updateUnit) {
        this.file = new File(path);
        this.defaultData = defaultData;
        this.updateInterval = updateUnit != null ? updateUnit.toMillis(updateInterval) : updateInterval;
        readData();
        setup(defaultData);
    }

    @Override
    public @NotNull File getFile() {
        return file;
    }

    @Override
    public @NotNull DataObject getData() {
        return data;
    }

    @Override
    public @NotNull DataObject readData() {
        this.lastUpdate = System.currentTimeMillis();
        return this.data = JsonUtil.tryReadObject(file, DataObject::empty, unused -> createFileIfAbsent());
    }

    @Override
    public long getUpdateInterval() {
        return updateInterval;
    }

    @Override
    public long getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public synchronized void save() {
        try {
            JsonUtil.saveToFile(file, data.toPrettyString());
        } catch (final IOException e) {
            LOGGER.error(String.format("Could not write content to config correctly (%s).", file.getPath()), e);
        }
    }

    @Override
    public void createFileIfAbsent() {
        try {
            synchronized (file) {

                final String parent = file.getParent();

                if (parent != null) {
                    Files.createDirectories(Paths.get(parent));
                }

                file.createNewFile();
            }
        } catch (final IOException e) {
            LOGGER.error("Could not setup config correctly.", e);
            System.exit(1);
        }
    }

    public boolean isSet(@NotNull String key) {

        if (defaultData == null) {
            return data.hasKey(key);
        }

        if (!data.hasKey(key)) {
            return false;
        }

        final String value = retrieveData().getString(key);

        for (final Map.Entry<String, Object> entry : defaultData.toMap().entrySet())
            if (entry.getKey().equals(key))
                return !value.equals(entry.getValue());
        return false;
    }

    public void requestValues() {
        defaultData.toMap()
                .keySet()
                .forEach(this::requestValue);
    }

    public @NotNull String requestValue(@NotNull String key) {
        return !isSet(key) ? request(key) : getData().hasKey(key) ? getData().getString(key) : "";
    }

    private @NotNull String request(@NotNull String key) {
        LOGGER.warn("There is no {} set in the config ({}). Please enter a value:", key, file.getPath());

        final String value = SCANNER.nextLine();

        this.data.put(key, value);
        save();

        return value;
    }

    private void setup(@Nullable DataObject data) {
        createFileIfAbsent();
        setupDefaultEntries(data);
    }

    private void setupDefaultEntries(@Nullable DataObject data) {

        if (data == null) {
            save();
            return;
        }

        if (data.keys().isEmpty()) {
            return;
        }

        defaultData.toMap().forEach((key, value) -> {

            if (this.data.hasKey(key) && !this.data.isNull(key)) {
                return;
            }

            this.data.put(key, value);
        });

        save();
    }

}
