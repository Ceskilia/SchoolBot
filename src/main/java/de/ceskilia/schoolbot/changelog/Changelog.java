package de.ceskilia.schoolbot.changelog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Changelog {

    private static final String COMMENT = "///";

    private final List<Feature> features;

    public static @NotNull Changelog fromFile(@NotNull String fileName) {
        try {
            return new Changelog(Files.readAllLines(Paths.get(fileName)).stream()
                    .filter(line -> !line.isBlank())
                    .filter(line -> !line.startsWith(COMMENT))
                    .map(Feature::parseFeature)
                    .toList()
            );
        } catch (final IOException e) {
            throw new RuntimeException("Could not parse file: " + fileName, e);
        }
    }

    public Changelog(@NotNull List<Feature> features) {
        this.features = Collections.unmodifiableList(features);
    }

    public Changelog(@NotNull Feature... features) {
        this(Arrays.asList(features));
    }

    public @NotNull List<Feature> getFeatures() {
        return features;
    }

    @Override
    public @NotNull String toString() {

        if (features.isEmpty()) {
            return "No changes yet.";
        }

        final StringBuilder builder = new StringBuilder();
        Feature.Type lastType = null;

        for (final Feature feature : features.stream()
                .sorted(Comparator.comparingInt(o -> o.getType().getKey()))
                .toList()) {
            final Feature.Type type = feature.getType();

            if (lastType == null || lastType != type) {

                if (lastType != null) {
                    builder.append("\n");
                }

                lastType = type;
            }

            builder.append(feature).append("\n");
        }

        return builder.toString();
    }

}
