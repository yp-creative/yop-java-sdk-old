package com.yeepay.g3.sdk.yop.http;

import com.yeepay.g3.frame.yop.ca.utils.Exceptions;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2017/1/24 下午12:38
 */
public class HttpClientUtils {

    public static CloseableHttpClient acceptsUntrustedCertsHttpClient(int connectTimeout, int readTimeout) throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

        // setup a Trust Strategy
        SSLContext sslContext = getSSLContext();
        httpClientBuilder.setSslcontext(sslContext);

        // check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        X509HostnameVerifier hostnameVerifier = SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our trust strategy;
        //      -- and create a Registry, to register it.
        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
                new String[]{"TLSv1"},
                null,
                hostnameVerifier);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        // TODO 生效？！
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
        connMgr.setMaxTotal(200);           // 总连接数
        connMgr.setDefaultMaxPerRoute(100); // 同路由的并发数
        httpClientBuilder.setConnectionManager(connMgr);

//        httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(3, false));

        RequestConfig.Builder builder = RequestConfig.custom();
        builder.setConnectionRequestTimeout(200); // ConnectionRequestTimeout used when requesting a connection from the connection manager.
        builder.setConnectTimeout(connectTimeout);// Connection timeout is the timeout until a connection with the server is established.
        builder.setSocketTimeout(readTimeout);
        RequestConfig requestConfig = builder.build();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        // finally, build the HttpClient;
        //      -- done!
        CloseableHttpClient client = httpClientBuilder.build();
        return client;
    }

    private static SSLContext getSSLContext() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return 1 == 1 ? getYeepaySSLContext() : getTrustAllSSLContext();
    }

    public static SSLContext getTrustAllSSLContext() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();
        return sslContext;
    }

    private static SSLContext getYeepaySSLContext() {
        SSLContext sslcontext = null;
        try {
            char[] password = "1".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream instream = new FileInputStream(new File("/Users/dreambt/Yeepay/yop-java-sdk-old/src/test/resources/cer/test1.pfx"));
            try {
                keyStore.load(instream, password);
            } finally {
                instream.close();
            }

            // Trust own CA and all self-signed certs
            sslcontext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, password)
                    .build();
        } catch (Exception e) {
            throw Exceptions.unchecked(e);
        }
        return sslcontext;
    }

}
