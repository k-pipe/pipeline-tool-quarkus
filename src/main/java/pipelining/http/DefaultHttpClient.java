package pipelining.http;

import pipelining.logging.Log;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static pipelining.application.Application.fail;
import static pipelining.logging.Log.onException;

public class DefaultHttpClient implements HttpClientFacade {

	private static final Map<Integer, HttpClient> connectTimeout2HttpClient = new HashMap<>();

	private static HttpClient getClient(HttpSettings settings) {
		HttpClient res = connectTimeout2HttpClient.get(settings.connectTimeOut);
		if (res == null) {
			final HttpClient.Builder clientBuilder = HttpClient.newBuilder();
			if (settings.connectTimeOut != null) {
				clientBuilder.connectTimeout(Duration.ofMillis(settings.connectTimeOut));
			}
			res = clientBuilder.build();
			connectTimeout2HttpClient.put(settings.connectTimeOut, res);
		}
		return res;
	}

	public <B> CompletableFuture<HttpResult> request(
			final HttpSettings settings,
			final String method,
			final String url,
			final B body) {
		try {
			HttpRequest.Builder request = HttpRequest.newBuilder(new URI(url))
					.method(method, getBodyPublisher(body));
			if (settings.responseTimeOut != null) {
				request.timeout(Duration.ofMillis(settings.responseTimeOut));
			}
			if (settings.headers != null) {
				settings.headers.forEach(request::setHeader);
			}
			request.version(settings.protocol == HttpVersion.HTTP1 ? HttpClient.Version.HTTP_1_1 : HttpClient.Version.HTTP_2);
			HttpClient client = getClient(settings);
			CompletableFuture<HttpResponse<InputStream>> future = client.sendAsync(request.build(),
					HttpResponse.BodyHandlers.ofInputStream());
			return future
					.whenComplete((r, e) -> close(body))
					.thenApply(this::createHttpResult);
		} catch (Exception e) {
			close(body);
			return CompletableFuture.failedFuture(e);
		}
	}

	private <B> HttpRequest.BodyPublisher getBodyPublisher(final B body) {
		if (body == null) {
			return HttpRequest.BodyPublishers.noBody();
		}
		if (body instanceof InputStream) {
			return HttpRequest.BodyPublishers.ofInputStream(() -> (InputStream) body);
		}
		if (body instanceof String) {
			return HttpRequest.BodyPublishers.ofString((String) body);
		}
		if (body instanceof byte[]) {
			return HttpRequest.BodyPublishers.ofByteArray((byte[]) body);
		}
		return fail("unexpected body type, only InputStream, String, byte[] are supported");
	}

	private HttpResult createHttpResult(final HttpResponse<InputStream> response) {
		return new HttpResult(response.statusCode(), response.body());
	}

	private <B> void close(final B body) {
		if ((body instanceof InputStream)) {
			Log.onException(((InputStream)body)::close).warn("Could not close input stream");
		}
	}

}
