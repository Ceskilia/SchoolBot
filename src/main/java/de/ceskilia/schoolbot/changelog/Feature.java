package de.ceskilia.schoolbot.changelog;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Feature {

    private final String message;
    private final Type type;

    public static @NotNull Feature parseFeature(@NotNull String line) {

        if (line.isBlank()) {
            throw new IllegalArgumentException("Blank line cannot be parsed.");
        }

        final Type type = Type.fromSymbol(line.trim().charAt(0));
        final String message = line.trim().substring(line.indexOf(type.symbol) + 1).trim();

        if (message.isBlank()) {
            throw new IllegalArgumentException("Blank message cannot be parsed.");
        }

        return new Feature(
                message,
                type
        );
    }

    public Feature(@NotNull String message, @NotNull Type type) {
        this.message = message;
        this.type = type;
    }

    public @NotNull String getMessage() {
        return message;
    }

    public @NotNull Type getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Feature feature = (Feature) o;
        return Objects.equals(message, feature.message) && type == feature.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, type);
    }

    @Override
    public String toString() {
        return type.getSymbol() + " " + message;
    }

    public enum Type {

        ADDED(1, '+'),
        REMOVED(2, '-'),
        UPDATED(3, '*');

        private final int key;
        private final char symbol;

        public static @NotNull Type fromSymbol(char symbol) {
            for (final Type type : Type.values()) {
                if (type.symbol == symbol) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Could not parse symbol: " + symbol);
        }

        Type(int key, char symbol) {
            this.key = key;
            this.symbol = symbol;
        }

        public int getKey() {
            return key;
        }

        public char getSymbol() {
            return symbol;
        }

    }

}
