package de.ceskilia.schoolbot.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class MapAction<I, O> implements CompletableAction<O> {

    private final CompletableAction<I> action;
    private final Function<? super I, ? extends O> mapper;

    public MapAction(@NotNull CompletableAction<I> action, @NotNull Function<? super I, ? extends O> mapper) {
        this.action = action;
        this.mapper = mapper;
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Predicate<? super Throwable> failure) {
        action.queue(result -> {
            if (success != null)
                success.accept(mapper.apply(result));
        }, failure);
    }

    @Override
    public @NotNull O complete() {
        return mapper.apply(action.complete());
    }

}
