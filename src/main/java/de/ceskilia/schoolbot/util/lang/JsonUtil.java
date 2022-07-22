package de.ceskilia.schoolbot.util.lang;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import net.dv8tion.jda.api.exceptions.ParsingException;
import net.dv8tion.jda.api.utils.data.*;
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

    public static @NotNull DataObject convertXmlToJson(@NotNull String xml) throws JsonProcessingException {
        return DataObject.fromJson(XML_MAPPER.readTree(xml).toString());
    }

    public static @NotNull DataObject tryReadObject(@NotNull File file,
                                                    @NotNull Supplier<? extends DataObject> fallback,
                                                    @Nullable Consumer<Exception> failure) {
        try(final FileInputStream inputStream = new FileInputStream(file)) {
            return DataObject.fromJson(inputStream);
        } catch (final IOException | ParsingException exception) {
            if(failure != null)
                failure.accept(exception);
            return fallback.get();
        }
    }

    public static @NotNull DataArray tryReadArray(@NotNull File file,
                                                  @NotNull Supplier<? extends DataArray> fallback,
                                                  @Nullable Consumer<Exception> failure) {
        try(final FileInputStream inputStream = new FileInputStream(file)) {
            return DataArray.fromJson(inputStream);
        } catch (final IOException | ParsingException exception) {
            if(failure != null)
                failure.accept(exception);
            return fallback.get();
        }
    }

    public static boolean containsElement(@NotNull DataArray array, @Nullable Object element) {

        if(element == null) {
            return false;
        }

        for(final Object entry : array)
            if(entry.equals(element))
                return true;
        return false;
    }

    public static void saveToFile(@Nullable File file, @Nullable String value) throws IOException {
        if(file == null || value == null)
            return;
        Files.writeString(file.toPath(), value);
    }

    public static @NotNull String toPrettyText(@NotNull Object object) {

        final StringBuilder builder = new StringBuilder();

        if(object instanceof SerializableData data) {
            builder.append(data.toData());
        } else if(object instanceof SerializableArray array) {
            builder.append(array.toDataArray().stream(DataArray::getString)
                    .collect(Collectors.joining(", "))
            );
        } else {
            builder.append(object);
        }

        return builder.toString();
    }

    public static @NotNull String safeToText(@Nullable DataObject data, @NotNull String key) {
        return data == null ? "" : data.hasKey(key) ? data.getString(key) : "";
    }

    public static @NotNull List<String> safeToList(@Nullable DataObject data, @NotNull String key) {
        return data == null ? Collections.emptyList() : data.hasKey(key) ? Arrays.asList(data.getString(key).split(", ")) : Collections.emptyList();
    }

    public static DataObject safeToObject(@Nullable DataObject data, @NotNull String key, @Nullable Supplier<DataObject> fallback) {
        return data == null ? fallback != null ? fallback.get() : null : data.hasKey(key) ? data.getObject(key) : fallback != null ? fallback.get() : null;
    }

    public static DataArray safeToArray(@Nullable DataObject data, @NotNull String key, @Nullable Supplier<DataArray> fallback) {
        return data == null ? fallback != null ? fallback.get() : null : data.hasKey(key) && data.isType(key, DataType.ARRAY) ? data.getArray(key) : fallback != null ? fallback.get() : null;
    }

}
