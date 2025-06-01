package com.kneissler.util.auth;

import java.util.Map;
import java.util.function.Consumer;

public interface Authentication<U> {

	String generateToken(Map<String, String> fields, String subject, String issuer, String audience, int expiresMinutes);

	void validate(String token, Consumer<U> userConsumer);

}
