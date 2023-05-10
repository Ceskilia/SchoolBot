package de.ceskilia.schoolbot.action;

import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class CompletableActionImpl<T> implements CompletableAction<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompletableActionImpl.class);
    private static Consumer<Throwable> DEFAULT_FAILURE = throwable -> LOGGER.error("A request resulted in an exception.", throwable);

    private final OkHttpClient client;
    private final String url;
    private final T defaultValue;
    private final Function<Response, T> mapper;

    public static @NotNull Consumer<Throwable> getDefaultFailure() {
        return DEFAULT_FAILURE;
    }

    public static void setDefaultFailure(@Nullable Consumer<Throwable> failure) {
        if (failure != null) {
            DEFAULT_FAILURE = failure;
        }
    }

    public CompletableActionImpl(@NotNull OkHttpClient client,
                                 @NotNull String url,
                                 @Nullable T defaultValue,
                                 @NotNull Function<Response, T> mapper) {
        this.client = client;
        this.url = url;
        this.defaultValue = defaultValue;
        this.mapper = mapper;
    }

    @Override
    public void queue(@Nullable Consumer<? super T> success, @Nullable Predicate<? super Throwable> failure) {
        logAttempt(Action.QUEUE);

        if (defaultValue != null) {
            if (success != null)
                success.accept(defaultValue);
            return;
        }

        client.newCall(new Request.Builder()
                .url(url)
                .build()
        ).enqueue(new Callback() {

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException exception) {
                acceptFailure(failure, exception);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                try (response) {
                    logResponse(Action.QUEUE, response);

                    if (!response.isSuccessful()) {
                        acceptFailure(failure, new ErrorResponseException(response));
                        return;
                    }

                    if (success != null) {
                        success.accept(mapper.apply(response));
                    }

                }
            }

        });
    }

    @Override
    public @NotNull T complete() {
        logAttempt(Action.COMPLETE);

        if (defaultValue != null) {
            return defaultValue;
        }

        try (final Response response = client.newCall(new Request.Builder()
                .url(url)
                .build()
        ).execute()) {
            logResponse(Action.COMPLETE, response);
            if (!response.isSuccessful()) throw new ErrorResponseException(response);
            return mapper.apply(response);
        } catch (final IOException exception) {
            throw new CompletionException("Could not complete action.", exception);
        }

    }

    public @NotNull OkHttpClient getClient() {
        return client;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public @Nullable T getDefaultValue() {
        return defaultValue;
    }

    public @NotNull Function<Response, T> getMapper() {
        return mapper;
    }

    public void acceptFailure(@Nullable Predicate<? super Throwable> failure, @NotNull Throwable throwable) {
        if (failure == null || failure.negate().test(throwable)) {
            DEFAULT_FAILURE.accept(throwable);
        }
    }

    private void logAttempt(@NotNull Action action) {
        LOGGER.debug("Attempting to {} an action with url {}{}.", action.getName(), url, defaultValue != null ? String.format(" and default value %s", defaultValue) : "");
    }

    private void logResponse(@NotNull Action action, @NotNull Response response) {
        LOGGER.debug("Received a response for a {} action with url {}. ({})", action.getMeaning(), url, response);
    }

    private enum Action {

        QUEUE("queue", "queued"),
        COMPLETE("complete", "completed");

        private final String name;
        private final String meaning;

        Action(@NotNull String name, @NotNull String meaning) {
            this.name = name;
            this.meaning = meaning;
        }

        public @NotNull String getName() {
            return name;
        }

        public @NotNull String getMeaning() {
            return meaning;
        }

    }

}
