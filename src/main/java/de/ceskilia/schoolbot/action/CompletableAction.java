package de.ceskilia.schoolbot.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface CompletableAction<T> {

    /**
     * The default failure callback used when none is provided in {@link #queue(Consumer, Predicate)}.
     *
     * @return the fallback consumer
     */
    static @NotNull Consumer<Throwable> getDefaultFailure() {
        return CompletableActionImpl.getDefaultFailure();
    }

    /**
     * The default failure callback used when none is provided in {@link #queue(Consumer, Predicate)}.
     *
     * @param failure the fallback to use
     */
    static void setDefaultFailure(@NotNull Consumer<Throwable> failure) {
        CompletableActionImpl.setDefaultFailure(failure);
    }

    /**
     * Submits a request for execution.
     *
     * <p><b>This method is asynchronous</b>
     *
     * @param success the success callback that will be called at a convenient time for the API
     * @param failure the failure callback that will be called if the request encounters an exception
     *                at its execution point
     */
    void queue(@Nullable Consumer<? super T> success, @Nullable Predicate<? super Throwable> failure);

    /**
     * Submits a request for execution using the default failure callback function.
     * To handle failures use {@link #queue(Consumer, Predicate)}.
     *
     * <p><b>This method is asynchronous</b>
     *
     * @param success the success callback that will be called at a convenient time for the API
     */
    default void queue(@Nullable Consumer<? super T> success) {
        queue(success, null);
    }

    /**
     * Submits a request for execution using no callback for success and the default failure callback function.
     * To handle success and failures use {@link #queue(Consumer, Predicate)}.
     *
     * <p><b>This method is asynchronous</b>
     */
    default void queue() {
        queue(null, null);
    }

    /**
     * Blocks the current thread and awaits the completion of the request.
     * <br>Used for synchronous logic.
     *
     * @return the response value
     */
    @NotNull T complete();

    /**
     * Intermediate operator that returns a modified CompletableAction.
     *
     * <p>This does not modify this instance but returns a new CompletableAction which will apply
     * the map function on successful execution. This will compute the result of both Actions.
     * <br>The returned CompletableAction must not be null!
     *
     * @param mapper the mapping function to apply to the action result, must return a CompletableAction
     * @param <O>    the target output type
     * @return CompletableAction for the mapped type
     */
    default <O> @NotNull FlatMapAction<T, O> flatMap(@NotNull Function<? super T, ? extends CompletableAction<O>> mapper) {
        return new FlatMapAction<>(this, mapper);
    }

    /**
     * Intermediate operator that returns a modified CompletableAction.
     *
     * <p>This does not modify this instance but returns a new CompletableAction which will apply
     * the map function on successful execution.
     *
     * @param mapper the mapping function to apply to the action result
     * @param <O>    the target output type
     * @return CompletableAction for the mapped type
     */
    default <O> @NotNull MapAction<T, O> map(@NotNull Function<? super T, ? extends O> mapper) {
        return new MapAction<>(this, mapper);
    }

}
