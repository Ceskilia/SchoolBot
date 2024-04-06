package de.ceskilia.schoolbot.util.lang;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public final class CollectionUtil {

    private CollectionUtil() {
        throw new UnsupportedOperationException("Instantiation of this utility class is unsupported.");
    }

    public static <E, C extends Collection<E>> @NotNull C replaceElements(
            @NotNull C oldCollection,
            @NotNull C newCollection
    ) {
        oldCollection.clear();
        oldCollection.addAll(newCollection);
        return oldCollection;
    }

}
