package de.ceskilia.schoolbot.school.verification;

import de.ceskilia.schoolbot.config.DefaultConfig;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import okhttp3.Credentials;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class VerificationManager {

    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String VERIFIED_USERS = "verifiedUsers";
    public static final String BLACKLISTED_USERS = "blacklistedUsers";

    private static final Logger LOGGER = LoggerFactory.getLogger(VerificationManager.class);

    private final DefaultConfig config;
    private String invalidCredentials;
    private boolean authorized;

    private final Set<Long> verifiedUsers;
    private final Set<Long> blacklistedUsers;

    public VerificationManager() {
        this.config = new DefaultConfig("config/verification.json", DataObject.empty()
                .put("username", "<USERNAME HERE>")
                .put("password", "<PASSWORD HERE>")
        );
        this.verifiedUsers = fetchUsers(VERIFIED_USERS);
        this.blacklistedUsers = fetchUsers(BLACKLISTED_USERS);
    }

    public @NotNull DefaultConfig getConfig() {
        return config;
    }

    public @NotNull Set<Long> getVerifiedUsers() {
        return Collections.unmodifiableSet(this.verifiedUsers);
    }

    public boolean isVerified(long userId) {
        return verifiedUsers.contains(userId);
    }

    public @NotNull VerificationResult tryVerify(long userId, @NotNull String username, @NotNull String password) {

        if (verifiedUsers.contains(userId)) {
            return VerificationResult.ALREADY_VERIFIED;
        }

        final DataObject data = config.retrieveData();
        final boolean rightUsername = username.equals(data.getString(USERNAME));
        final boolean rightPassword = password.equals(data.getString(PASSWORD));

        if (rightUsername && rightPassword) {
            verifiedUsers.add(userId);
            data.put(VERIFIED_USERS, DataArray.fromCollection(verifiedUsers));
            config.save();
            return VerificationResult.SUCCEED;
        }

        return !rightUsername ? !rightPassword ? VerificationResult.WRONG_DATA : VerificationResult.WRONG_USERNAME : VerificationResult.WRONG_PASSWORD;
    }

    public @NotNull Set<Long> getBlacklistedUsers() {
        return Collections.unmodifiableSet(this.blacklistedUsers);
    }

    public boolean isBlacklisted(long userId) {
        return blacklistedUsers.contains(userId);
    }

    public boolean updateUserBlacklist(long userId) {
        boolean blacklist = false;

        if (!this.blacklistedUsers.remove(userId)) {
            blacklist = this.blacklistedUsers.add(userId);
        }

        config.getData().put(BLACKLISTED_USERS, DataArray.fromCollection(blacklistedUsers));
        config.save();
        return blacklist;
    }

    public @NotNull String getCredentials() {
        final DataObject data = config.retrieveData();

        config.checkValue(USERNAME);
        config.checkValue(PASSWORD);

        return Credentials.basic(data.getString(USERNAME), data.getString(PASSWORD));
    }

    public @Nullable String getInvalidCredentials() {
        return invalidCredentials;
    }

    public boolean isAuthorized() {
        return authorized;
    }

    public void authorized(boolean authorized) {
        this.authorized = authorized;

        if (!authorized) {
            this.invalidCredentials = getCredentials();
        }

    }

    public boolean canRequest() {
        return this.invalidCredentials == null || (isAuthorized() && !getCredentials().equals(this.invalidCredentials));
    }

    private @NotNull Set<Long> fetchUsers(@NotNull String key) {
        final DataObject data = config.getData();

        if (!data.hasKey(key))
            return new HashSet<>();
        return data.getArray(key)
                .stream(DataArray::getLong)
                .collect(Collectors.toSet());
    }

}
