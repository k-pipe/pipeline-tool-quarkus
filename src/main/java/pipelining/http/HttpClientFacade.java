package pipelining.http;

import java.util.concurrent.CompletableFuture;

public interface HttpClientFacade {
	<B> CompletableFuture<HttpResult> request(
			HttpSettings settings,
			String method,
			String url,
			B body);

}