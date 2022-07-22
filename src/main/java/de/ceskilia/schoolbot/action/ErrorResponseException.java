package de.ceskilia.schoolbot.action;

import de.ceskilia.cutils.utils.util.NumberUtil;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

public class ErrorResponseException extends RuntimeException {

    public static final int NOT_FOUND = 404;
    public static final int UNAUTHORIZED = 401;

    private final Response response;
    private final String meaning;
    private final int code;

    public ErrorResponseException(@NotNull Response response) {
        super(String.format("Received an unsuccessful response: %s", response));
        this.response = response;
        this.meaning = response.message();
        this.code = response.code();
    }

    public @NotNull Response getResponse() {
        return response;
    }

    public @NotNull String getMeaning() {
        return meaning;
    }

    public int getCode() {
        return code;
    }

    public boolean isInformational() {
        return NumberUtil.inRange(code,100,199);
    }

    public boolean isRedirection() {
        return NumberUtil.inRange(code,300,399);
    }

    public boolean isClientError() {
        return NumberUtil.inRange(code,400,499);
    }

    public boolean isServerError() {
        return NumberUtil.inRange(code,500,5599);
    }

}
