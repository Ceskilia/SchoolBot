package de.ceskilia.schoolbot.changelog;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Feature {

    private final String message;
    private final Type type;

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

        ADDED(1,'+'),
        REMOVED(2, '-'),
        UPDATED(3,'*');

        private final int key;
        private final char symbol;

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
