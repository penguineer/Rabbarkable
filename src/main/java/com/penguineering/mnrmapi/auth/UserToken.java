package com.penguineering.mnrmapi.auth;

import java.time.Duration;
import java.time.Instant;

public class UserToken {
    public static final Duration VALIDITY = Duration.ofHours(6);

    public static UserToken withToken(String token) {
        return new UserToken(token, VALIDITY);
    }

    private final String token;
    private final Instant expires;

    UserToken(String token, Duration validity) {
        this.token = token;
        this.expires = Instant.now().plus(validity);
    }

    public String getToken() {
        return token;
    }

    public Instant getExpires() {
        return expires;
    }

    public boolean isValid() {
        return Instant.now().isBefore(this.expires);
    }

    @Override
    public String toString() {
        return "User token (expiry %s): %s".formatted(this.expires, this.token);
    }
}
