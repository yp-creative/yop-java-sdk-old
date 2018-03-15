package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.core.yop.utils.test.benchmark.BenchmarkTask;
import com.yeepay.g3.core.yop.utils.test.benchmark.ConcurrentBenchmark;
import com.yeepay.g3.sdk.yop.client.YopClient;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import com.yeepay.g3.sdk.yop.client.YopResponse;
import org.apache.commons.lang3.time.StopWatch;

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
            // RandomStringUtils.randomAlphanumeric(20)
            YopRequest request = new YopRequest("yop-boss", "PdZ74F6sxapgOWJ31QKmYw==");
            request.setSignAlg("SHA-256");

            if (random.nextInt(100) >= 20) {// 20% 的请求超过30s
                request.addParam("backendLatency", "10000");
            } else {
                request.addParam("backendLatency", "20000");
            }

            YopResponse response = null;
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                response = YopClient.post("/rest/v1.0/yop/mock/backend-latency", request);
//                assertTrue(response.isSuccess());
//                assertTrue(response.isValidSign());
                stopWatch.stop();
                System.out.println("stopWatch:" + stopWatch.getTime() + "\t" + response.isValidSign());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
