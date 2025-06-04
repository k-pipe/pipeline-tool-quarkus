package pipelining.http;

import pipelining.application.Retry;
import pipelining.application.RetryableResult;
import pipelining.util.Utf8;
import pipelining.logging.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class Http {

	private static HttpClientFacade client = new DefaultHttpClient();
	private static HttpSettings defaultSettings = new HttpSettings();

	// server: static methods

	public static void setClient(HttpClientFacade client) {
		if ((Http.client != null) && !(Http.client instanceof DefaultHttpClient)) {
			Log.warn("HttpClient was set twice");
		}
		Http.client = client;
	}

	public static void setDefaultSettings(HttpSettings settings) {
		defaultSettings = settings;
	}

	// methods for uploading/downloading files

	public static boolean download(final HttpSettings settings, final String url, final Path path) {
		return retryRequest(settings, "GET", url, () -> null, new CopyToFile(path)).isPresent();
	}

	public static boolean upload(final HttpSettings settings, final String url, final Path file) {
		return retryRequest(settings, "PUT", url, new FileBody(file), new ResponseDebug()).isPresent();
	}

	public static boolean download(final String url, final Path path) {
		return download(defaultSettings, url, path);
	}

	public static boolean upload(final String url, final Path path) {
		return upload(defaultSettings, url, path);
	}

	// methods for streaming

	public static boolean streamDown(final HttpSettings settings, final String url, final OutputStream out) {
		return retryRequest(settings, "GET", url, () -> null, new StreamDown(out)).isPresent();
	}

	public static boolean streamUp(final HttpSettings settings, final String url, final InputStream in) {
		return retryRequest(settings, "PUT", url, new InputStreamBody(in), new ResponseDebug()).isPresent();
	}

	public static boolean streamDown(final String url, final OutputStream out) {
		return streamDown(defaultSettings, url, out);
	}

	public static boolean streamUp(final String url, final InputStream in) {
		return streamUp(defaultSettings, url, in);
	}

	// blocking static methods for major Http-Methods

	public static <R> Optional<R> get(final HttpSettings settings, final String url, StreamReader<R> reader) {
		return retryRequest(settings, "GET", url, () -> null, reader);
	}

	public static <B, R> Optional<R> put(final HttpSettings settings, final String url, Supplier<B> bodySupplier, StreamReader<R> reader) {
		return retryRequest(settings, "PUT", url, bodySupplier, reader);
	}

	public static <B, R> Optional<R> post(final HttpSettings settings, final String url, Supplier<B> bodySupplier, StreamReader<R> reader) {
		return retryRequest(settings, "POST", url, bodySupplier, reader);
	}

	public static <B, R> Optional<R> patch(final HttpSettings settings, final String url, Supplier<B> bodySupplier, StreamReader<R> reader) {
		return retryRequest(settings, "PATCH", url, bodySupplier, reader);
	}

	public static boolean delete(final HttpSettings settings, final String url) {
		return retryRequest(settings, "DELETE", url, () -> null, new ResponseDebug()).isPresent();
	}

	public static boolean delete(final String url) {
		return delete(defaultSettings, url);
	}

	// blocking static methods for strings

	public static Optional<String> get(final HttpSettings settings, final String url) {
		return get(settings, url, Utf8::read);
	}

	public static Optional<String> put(final HttpSettings settings, final String url, String body) {
		return put(settings, url, () -> body, Utf8::read);
	}

	public static Optional<String> post(final HttpSettings settings, final String url, String body) {
		return post(settings, url, () -> body, Utf8::read);
	}

	public static Optional<String> patch(final HttpSettings settings, final String url, String body) {
		return patch(settings, url, () -> body, Utf8::read);
	}

	// blocking static methods for strings with default settings

	public static Optional<String> get(final String url) {
		return get(defaultSettings, url);
	}

	public static Optional<String> put(final String url, final String body) {
		return put(defaultSettings, url, body);
	}

	public static Optional<String> post(final String url, final String body) {
		return post(defaultSettings, url, body);
	}


	// helper methods

	private static <B, R> Optional<R> retryRequest(
			HttpSettings settings,
			String method,
			String url,
			Supplier<B> bodySupplier,
			StreamReader<R> reader) {
		return new Retry(settings.maxNumRetries, settings.initialRetryWaitMilis, settings.increaseWaitFactor)
				.withRetryableSupplier(() -> tryReadResult(settings, client.request(settings, method, url, bodySupplier.get()), reader, method, url));
	}

	private static <R> RetryableResult<R> tryReadResult(final HttpSettings settings,
                                                        final CompletableFuture<HttpResult> future,
                                                        StreamReader<R> reader,
                                                        String method,
                                                        String url) {
		HttpRetryable<R> res;
		HttpResult httpRes;
		try {
			httpRes = future.get();
			res = new HttpRetryable<>(settings, httpRes);
			if (res.hasSucceeded()) {
				try {
					res.setResult(reader.read(httpRes.getBody()));
				} catch (IOException e) {
					res.readingFailed(e);
				} finally {
					httpRes.getBody().close();
				}
			}
		} catch (Throwable t) {
			httpRes = new HttpResult(t);
			res = new HttpRetryable<>(t);
		}
		if (res.shouldRetry()) {
			callRetryListeners(settings, httpRes, method, url, res);
		}
		return res;
	}

	private static <R> void callRetryListeners(HttpSettings settings, HttpResult httpResult, String method, String url, HttpRetryable<R> retryable) {
		settings.retryListeners.forEach(rl -> {
			if (rl.vetoRetry(method, url, httpResult.getStatus(), httpResult.getException())) {
				retryable.vetoShouldRetry();
			}
		});
	}

}
