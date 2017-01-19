package com.yeepay.g3.sdk.yop.http;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * title: Http Utils<br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/18 16:45
 */
public final class HttpUtils {

    private static final Logger LOGGER = Logger.getLogger(HttpUtils.class);

    private static BitSet URI_UNRESERVED_CHARACTERS = new BitSet();
    private static String[] PERCENT_ENCODED_STRINGS = new String[256];

    private static final Joiner queryStringJoiner = Joiner.on('&');
    private static boolean HTTP_VERBOSE = Boolean.parseBoolean(System.getProperty("yop.sdk.http", "false"));

    private static final int PORT_LOWER_BOUND = 1;
    private static final int PORT_UPPER_BOUND = 65535;

    private HttpUtils() {
        // do nothing
    }

    /**
     * Regex which matches any of the sequences that we need to fix up after URLEncoder.encode().
     */
    static {
        for (int i = 'a'; i <= 'z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        for (int i = '0'; i <= '9'; i++) {
            URI_UNRESERVED_CHARACTERS.set(i);
        }
        URI_UNRESERVED_CHARACTERS.set('-');
        URI_UNRESERVED_CHARACTERS.set('.');
        URI_UNRESERVED_CHARACTERS.set('_');
        URI_UNRESERVED_CHARACTERS.set('~');

        for (int i = 0; i < PERCENT_ENCODED_STRINGS.length; ++i) {
            PERCENT_ENCODED_STRINGS[i] = String.format("%%%02X", i);
        }
    }

    /**
     * Normalize a string for use in url path. The algorithm is:
     * <p>
     * <p>
     * <ol>
     * <li>Normalize the string</li>
     * <li>replace all "%2F" with "/"</li>
     * <li>replace all "//" with "/%2F"</li>
     * </ol>
     * <p>
     * <p>
     * object key can contain arbitrary characters, which may result double slash in the url path. Apache http
     * client will replace "//" in the path with a single '/', which makes the object key incorrect. Thus we replace
     * "//" with "/%2F" here.
     *
     * @param path the path string to normalize.
     * @return the normalized path string.
     * @see #normalize(String)
     */
    public static String normalizePath(String path) {
        return normalize(path).replace("%2F", "/");
    }

    /**
     * Normalize a string for use in web service APIs. The normalization algorithm is:
     * <p>
     * <ol>
     * <li>Convert the string into a UTF-8 byte array.</li>
     * <li>Encode all octets into percent-encoding, except all URI unreserved characters per the RFC 3986.</li>
     * </ol>
     * <p>
     * <p>
     * All letters used in the percent-encoding are in uppercase.
     *
     * @param value the string to normalize.
     * @return the normalized string.
     * @throws UnsupportedEncodingException
     */
    public static String normalize(String value) {
        try {
            StringBuilder builder = new StringBuilder();
            for (byte b : value.getBytes("UTF-8")) {
                int bl = b & 0xFF;
                if (URI_UNRESERVED_CHARACTERS.get(bl)) {
                    builder.append((char) b);
                } else {
                    builder.append(PERCENT_ENCODED_STRINGS[bl]);
                }
            }
            return builder.toString();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a host header according to the specified URI. The host header is generated with the same logic used by
     * apache http client, that is, append the port to hostname only if it is not the default port.
     *
     * @param uri the URI
     * @return a host header according to the specified URI.
     */
    public static String generateHostHeader(URI uri) {
        String host = uri.getHost();
        if (isUsingNonDefaultPort(uri)) {
            host += ":" + uri.getPort();
        }
        return host;
    }

    /**
     * Returns true if the specified URI is using a non-standard port (i.e. any port other than 80 for HTTP URIs or any
     * port other than 443 for HTTPS URIs).
     *
     * @param uri the URI
     * @return True if the specified URI is using a non-standard port, otherwise false.
     */
    public static boolean isUsingNonDefaultPort(URI uri) {
        String scheme = uri.getScheme().toLowerCase();
        int port = uri.getPort();
        if (port < PORT_LOWER_BOUND || port > PORT_UPPER_BOUND) {
            return false;
        }
        if (scheme.equals(Protocol.HTTP.toString())) {
            return port != Protocol.HTTP.getDefaultPort();
        }
        if (scheme.equals(Protocol.HTTPS.toString())) {
            return port != Protocol.HTTPS.getDefaultPort();
        }
        return false;
    }

    public static String getCanonicalURIPath(String path) {
        if (path == null) {
            return "/";
        } else if (path.startsWith("/")) {
            return normalizePath(path);
        } else {
            return "/" + normalizePath(path);
        }
    }

    public static String getCanonicalQueryString(Map<String, String[]> parameters, boolean forSignature) {
        if (parameters.isEmpty()) {
            return "";
        }

        List<String> parameterStrings = Lists.newArrayList();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            String key = entry.getKey();
            checkNotNull(key, "parameter key should not be null");

            if (forSignature && Headers.AUTHORIZATION.equalsIgnoreCase(key)) {
                continue;
            }

            String[] value = entry.getValue();
            if (value.length == 0) {
                if (forSignature) {
                    parameterStrings.add(normalize(key) + '=');
                } else {
                    parameterStrings.add(normalize(key));
                }
            } else {
                for (String item : value) {
                    parameterStrings.add(normalize(key) + '=' + normalize(item));
                }
            }
        }
        Collections.sort(parameterStrings);

        return queryStringJoiner.join(parameterStrings);
    }

    /**
     * Append the given path to the given baseUri.
     * <p>
     * <p>
     * This method will encode the given path but not the given baseUri.
     *
     * @param baseUri
     * @param pathComponents
     */
    public static URI appendUri(URI baseUri, String... pathComponents) {
        StringBuilder builder = new StringBuilder(baseUri.toASCIIString());
        for (String path : pathComponents) {
            if (StringUtils.isNotEmpty(path)) {
                path = normalizePath(path);
                if (path.startsWith("/")) {
                    if (builder.charAt(builder.length() - 1) == '/') {
                        builder.setLength(builder.length() - 1);
                    }
                } else {
                    if (builder.charAt(builder.length() - 1) != '/') {
                        builder.append('/');
                    }
                }
                builder.append(path);
            }
        }
        try {
            return new URI(builder.toString());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Unexpected error", e);
        }
    }

    public static void printRequest(HttpRequestBase request) {
        if (!HTTP_VERBOSE) {
            return;
        }
        LOGGER.debug("\n-------------> ");
        LOGGER.debug(request.getRequestLine());
        for (Header h : request.getAllHeaders()) {
            LOGGER.debug(h.getName() + " : " + h.getValue());
        }
        RequestConfig config = request.getConfig();
        if (config != null) {
            LOGGER.debug("getConnectionRequestTimeout: "
                    + config.getConnectionRequestTimeout());
            LOGGER.debug("getConnectTimeout: "
                    + config.getConnectTimeout());
            LOGGER.debug("getCookieSpec: " + config.getCookieSpec());
            LOGGER.debug("getLocalAddress: " + config.getLocalAddress());

        }
    }

    public static void printResponse(CloseableHttpResponse response) {
        if (!HTTP_VERBOSE) {
            return;
        }
        LOGGER.debug("\n<------------- ");
        StatusLine status = response.getStatusLine();
        LOGGER.debug(status.getStatusCode() + " - "
                + status.getReasonPhrase());
        Header[] heads = response.getAllHeaders();
        for (Header h : heads) {
            LOGGER.debug(h.getName() + " : " + h.getValue());
        }
    }

}
