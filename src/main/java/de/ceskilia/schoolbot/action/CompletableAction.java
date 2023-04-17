package de.ceskilia.schoolbot.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface CompletableAction<T> {

    static @NotNull Consumer<Throwable> getDefaultFailure() {
        return CompletableActionImpl.getDefaultFailure();
    }

    static void setDefaultFailure(@NotNull Consumer<Throwable> failure) {
        CompletableActionImpl.setDefaultFailure(failure);
    }

    void queue(@Nullable Consumer<? super T> success, @Nullable Predicate<? super Throwable> failure);

    default void queue(@Nullable Consumer<? super T> success) {
        queue(success, null);
    }

    default void queue() {
        queue(null, null);
    }

    @NotNull T complete();

    default <O> @NotNull FlatMapAction<T, O> flatMap(@NotNull Function<? super T, ? extends CompletableAction<O>> mapper) {
        return new FlatMapAction<>(this, mapper);
    }

    default <O> @NotNull MapAction<T, O> map(@NotNull Function<? super T, ? extends O> mapper) {
        return new MapAction<>(this, mapper);
    }

}
