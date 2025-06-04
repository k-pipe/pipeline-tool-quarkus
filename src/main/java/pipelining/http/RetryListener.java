package pipelining.http;

public interface RetryListener {
    /**
     * Is called when a request failed before retry occurs
     *
     * @param method http method, "GET", "PUT", etc.
     * @param url request url
     * @param status http status returned
     * @param exception exception thrown while sending request or reading response
     * @return true if retry shall be votoed, if any retry listener vetos, the request is not retried and fails immediately
     */
    boolean vetoRetry(String method, String url, int status, Throwable exception);
}
