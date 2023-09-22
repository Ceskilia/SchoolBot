package de.ceskilia.schoolbot.config;

import de.ceskilia.schoolbot.util.lang.JsonUtil;
import net.dv8tion.jda.api.utils.data.DataArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class ArrayConfig implements Config<DataArray> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayConfig.class);

    private final File file;
    private final long updateInterval;

    private DataArray data;
    private long lastUpdate;

    protected ArrayConfig(@NotNull String path, long updateInterval, @Nullable TimeUnit updateUnit) {
        this.file = new File(path);
        this.updateInterval = updateUnit != null ? updateUnit.toMillis(updateInterval) : updateInterval;
        readData();
    }

    @Override
    public @NotNull File getFile() {
        return file;
    }

    @Override
    public @NotNull DataArray getData() {
        return data;
    }

    @Override
    public @NotNull DataArray readData() {
        this.lastUpdate = System.currentTimeMillis();
        return this.data = JsonUtil.tryReadArray(file, DataArray::empty, unused -> createFileIfAbsent());
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

    public boolean contains(@NotNull Object value) {
        for (final Object element : data)
            if (element.equals(value))
                return true;
        return false;
    }

}
