package de.ceskilia.schoolbot.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FlatMapAction<I, O> implements CompletableAction<O> {

    private final CompletableAction<I> action;
    private final Function<? super I, ? extends CompletableAction<O>> mapper;

    public FlatMapAction(@NotNull CompletableAction<I> action, @NotNull Function<? super I, ? extends CompletableAction<O>> mapper) {
        this.action = action;
        this.mapper = mapper;
    }

    @Override
    public void queue(@Nullable Consumer<? super O> success, @Nullable Predicate<? super Throwable> failure) {
        action.queue(result -> mapper.apply(result).queue(success, failure));
    }

    @Override
    public @NotNull O complete() {
        return mapper.apply(action.complete()).complete();
    }

}
