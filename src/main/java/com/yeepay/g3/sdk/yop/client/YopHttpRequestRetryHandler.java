/*
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */

package com.yeepay.g3.sdk.yop.client;

import com.google.common.collect.Sets;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.Args;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/10/8 3:21 PM
 */
public class YopHttpRequestRetryHandler implements HttpRequestRetryHandler {

    private final int retryCount;

    private final boolean requestSentRetryEnabled;

    private static final int SCALE_FACTOR = 300;

    private final Set<Class<? extends IOException>> nonRetriableClasses = Sets.newHashSet(
            InterruptedIOException.class,
            UnknownHostException.class,
            SocketException.class,
            ConnectException.class,
            SSLException.class);

    public YopHttpRequestRetryHandler() {
        this(3, false);
    }

    public YopHttpRequestRetryHandler(final int retryCount, final boolean requestSentRetryEnabled) {
        this.retryCount = retryCount;
        this.requestSentRetryEnabled = requestSentRetryEnabled;
    }

    @Override
    public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        Args.notNull(exception, "Exception parameter");
        Args.notNull(context, "HTTP context");
        if (executionCount > this.retryCount) {
            // Do not retry if over max retry count
            return false;
        }
        if (this.nonRetriableClasses.contains(exception.getClass())) {
            return false;
        } else {
            for (final Class<? extends IOException> rejectException : this.nonRetriableClasses) {
                if (rejectException.isInstance(exception)) {
                    return false;
                }
            }
        }

        if (retryRequestWithSleep(exception, executionCount, context)) {
            try {
                Thread.sleep(getDelayBeforeNextRetryInMillis(executionCount));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return true;
        }

        // otherwise do not retry
        return false;
    }

    private long getDelayBeforeNextRetryInMillis(int retriesAttempted) {
        return (1 << (retriesAttempted + 1)) * SCALE_FACTOR;
    }

    private boolean retryRequestWithSleep(IOException exception, int executionCount, HttpContext context) {
        if (exception instanceof ConnectTimeoutException || exception instanceof NoHttpResponseException
                || exception instanceof UnknownHostException || exception instanceof ConnectionPoolTimeoutException) {
            return true;
        }

        final HttpClientContext clientContext = HttpClientContext.adapt(context);
        final HttpRequest request = clientContext.getRequest();

        if (handleAsIdempotent(request)) {
            // Retry if the request is considered idempotent
            return true;
        }

        if (!clientContext.isRequestSent() || this.requestSentRetryEnabled) {
            // Retry if the request has not been sent fully or
            // if it's OK to retry methods that have been sent
            return true;
        }

        int statusCode = null != clientContext.getResponse() ? clientContext.getResponse().getStatusLine().getStatusCode() : 0;
        if (HttpStatus.SC_BAD_GATEWAY == statusCode) {
            return true;
        }

        return false;
    }

    /**
     * @return {@code true} if this handler will retry methods that have
     * successfully sent their request, {@code false} otherwise
     */
    public boolean isRequestSentRetryEnabled() {
        return requestSentRetryEnabled;
    }

    /**
     * @return the maximum number of times a method will be retried
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     * @since 4.2
     */
    protected boolean handleAsIdempotent(final HttpRequest request) {
        return !(request instanceof HttpEntityEnclosingRequest);
    }

}
