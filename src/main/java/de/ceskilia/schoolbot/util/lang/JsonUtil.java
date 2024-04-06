package de.ceskilia.schoolbot.util.lang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.ceskilia.config.data.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class JsonUtil {

    public static final XmlMapper XML_MAPPER = new XmlMapper();

    private JsonUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static <T> @NotNull T convertXml(@NotNull String xml, @NotNull Class<T> tClass) throws JsonProcessingException {
        return XML_MAPPER.readValue(xml, tClass);
    }

    public static @NotNull ConfigDataObject convertXmlToJson(@NotNull String xml) throws JsonProcessingException {
        return ConfigDataObject.fromJson(XML_MAPPER.readTree(xml).toString());
    }

    public static @NotNull ConfigDataObject tryReadObject(@NotNull File file,
                                                    @NotNull Supplier<? extends ConfigDataObject> fallback,
                                                    @Nullable Consumer<Exception> failure) {
        try (final FileInputStream inputStream = new FileInputStream(file)) {
            return ConfigDataObject.fromJson(inputStream);
        } catch (final IOException | ParsingException exception) {
            if (failure != null)
                failure.accept(exception);
            return fallback.get();
        }
    }

    public static @NotNull ConfigDataArray tryReadArray(@NotNull File file,
                                                        @NotNull Supplier<? extends ConfigDataArray> fallback,
                                                        @Nullable Consumer<Exception> failure) {
        try (final FileInputStream inputStream = new FileInputStream(file)) {
            return ConfigDataArray.fromJson(inputStream);
        } catch (final IOException | ParsingException exception) {
            if (failure != null)
                failure.accept(exception);
            return fallback.get();
        }
    }

    public static boolean containsElement(@NotNull ConfigDataArray array, @Nullable Object element) {

        if (element == null) {
            return false;
        }

        for (final Object entry : array)
            if (entry.equals(element))
                return true;
        return false;
    }

    public static void saveToFile(@Nullable File file, @Nullable String value) throws IOException {
        if (file == null || value == null)
            return;
        Files.writeString(file.toPath(), value);
    }

    public static @NotNull String toPrettyText(@NotNull Object object) {
        String result =
                (object instanceof SerializableConfigData data) ? data.toData().toString() :
                        (object instanceof SerializableConfigArray array) ? array.toDataArray().stream(ConfigDataArray::getString)
                                .collect(Collectors.joining(", "))
                                :
                                object.toString();
        return result.isBlank() ? "-" : result;
    }

    public static @NotNull String safeToText(@Nullable ConfigDataObject data, @NotNull String key) {
        return data == null ? "" : data.hasKey(key) ? data.getString(key) : "";
    }

    public static @NotNull List<String> safeToList(@Nullable ConfigDataObject data, @NotNull String key) {
        return data == null ? Collections.emptyList() : data.hasKey(key) ? Arrays.asList(data.getString(key).split(", ")) : Collections.emptyList();
    }

    public static @NotNull ConfigDataArray safeToArray(@Nullable ConfigDataObject data, @NotNull String key) {
        return safeToArray(data, key, null);
    }

    public static ConfigDataArray safeToArray(@Nullable ConfigDataObject data, @NotNull String key, @Nullable Supplier<ConfigDataArray> fallback) {

        if (data == null || !data.hasKey(key)) {
            return fallback != null ? fallback.get() : ConfigDataArray.empty();
        }

        if (data.isType(key, ConfigDataType.OBJECT)) {
            return ConfigDataArray.fromCollection(Collections.singleton(data.getObject(key)));
        }

        if (data.isType(key, ConfigDataType.ARRAY)) {
            return data.getArray(key);
        }

        return fallback != null ? fallback.get() : ConfigDataArray.empty();
    }

    public static ConfigDataObject safeToObject(@Nullable ConfigDataObject data, @NotNull String key, @Nullable Supplier<ConfigDataObject> fallback) {
        return data == null ?
                fallback != null ? fallback.get() : null
                :
                data.hasKey(key) ?
                        data.getObject(key)
                        :
                        fallback != null ? fallback.get() : null;
    }

}
