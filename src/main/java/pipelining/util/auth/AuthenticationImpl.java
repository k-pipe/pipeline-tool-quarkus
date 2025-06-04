package pipelining.util.auth;

/*
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.PubSecKeyOptions;
import io.vertx.ext.auth.User;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.jwt.JWTOptions;
*/
public class AuthenticationImpl {

/*	public AuthenticationImpl(Vertx vertx, JsonObject json) {
		this.auth = JWTAuth.create(vertx, new JWTAuthOptions().addPubSecKey(new PubSecKeyOptions(json)));
		this.algorithm = json.getString("algorithm");
	}

	public String generateToken(Map<String, String> fields, String subject, String issuer, String audience, int expiresMinutes) {
		JsonObject obj = new JsonObject();
		fields.forEach(obj::put);
		return auth.generateToken(obj, new JWTOptions()
				.setAlgorithm(algorithm)
				.setExpiresInMinutes(expiresMinutes)
				.setAudience(Arrays.asList(audience))
				.setIssuer(issuer)
				.setSubject(subject));
	}

	public void validate(String token, Handler<AsyncResult<User>> handler) {
		auth.authenticate(new JsonObject().put("jwt", token), handler); 
	}
*/
}
