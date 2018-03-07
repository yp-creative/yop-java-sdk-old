package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.core.yop.utils.test.benchmark.BenchmarkTask;
import com.yeepay.g3.core.yop.utils.test.benchmark.ConcurrentBenchmark;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 2018/3/1 下午12:34
 */
public class YopClientBenchmark extends ConcurrentBenchmark {

    private static final int DEFAULT_THREAD_COUNT = 50;
    private static final long DEFAULT_TOTAL_COUNT = 1000000;

    private static final SecureRandom random = new SecureRandom();

    public YopClientBenchmark() {
        super(DEFAULT_THREAD_COUNT, DEFAULT_TOTAL_COUNT);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("yop.sdk.config.file", "/config/yop_sdk_config_local.json");

        YopClientBenchmark benchmark = new YopClientBenchmark();
        benchmark.execute();
    }

    @Override
    protected BenchmarkTask createTask() {
        return new InvokeTask();
    }

    public class InvokeTask extends BenchmarkTask {
        @Override
        protected void execute(int requestSequence) {
            YopRequest request = new YopRequest(RandomStringUtils.randomAlphanumeric(20), "PdZ74F6sxapgOWJ31QKmYw==");
            request.setSignAlg("SHA-256");

//        if (random.nextInt(100) >= 0) {// 20% 的请求超过20s
//            request.addParam("backendLatency", "0");
//        } else {
            request.addParam("backendLatency", "200");
//        }

            YopResponse response = null;
            try {
                response = YopClient.post("/rest/v1.0/yop/mock/backend-latency", request);
//                assertTrue(response.isSuccess());
//                assertTrue(response.isValidSign());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
