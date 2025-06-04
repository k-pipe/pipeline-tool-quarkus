package pipelining.http;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Settings for Http Client
 *
 * Default values are set to retry for (2^(28/2) * 2 - 1) * 0.1s: approx. 1h
 */
public class HttpSettings {
	/* how many retries are made, excluding the first trial, a value of 0 means the request is tried once, no retries in case of failure */
	public int maxNumRetries = 28;

	/* delay in milli seconds between first trial and first retry */
	public long initialRetryWaitMilis = 100;

	/* the delay is increased by this factor after each unsuccessful retry */
	public double increaseWaitFactor = Math.sqrt(2);

	/* time in milli seconds to wait for a response, null = wait indefinitely */
	public Integer responseTimeOut = 600_000;

	/* time in milli seconds to wait for a connection, null = wait indefinitely */
	public Integer connectTimeOut = 10_000;

	/* version of the http protocol to use */
	public HttpVersion protocol = HttpVersion.HTTP1;

	/* Ranges of http status codes that are considered successful */
	public List<int[]> successStatusRanges = List.of(new int[] { 200, 299 });

	/* Ranges of http status codes that trigger a retry */
	public List<int[]> retryStatusRanges = List.of(new int[] { 500, 599 });

	/* Custom headers to be added to each request */
	public Map<String, String> headers = new LinkedHashMap<>();

	/* Custom headers to be added to each request */
	public List<RetryListener> retryListeners = new ArrayList<>();
}