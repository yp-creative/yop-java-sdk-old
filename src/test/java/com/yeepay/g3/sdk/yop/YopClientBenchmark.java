package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.core.yop.utils.test.benchmark.BenchmarkTask;
import com.yeepay.g3.core.yop.utils.test.benchmark.ConcurrentBenchmark;
import com.yeepay.g3.sdk.yop.client.YopRequest;
import org.apache.commons.lang3.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(YopClientBenchmark.class);

    private static final int DEFAULT_THREAD_COUNT = 30;
    private static final long DEFAULT_TOTAL_COUNT = 1000000;

    private static final SecureRandom random = new SecureRandom();

    private static final LocalDemo demo = new LocalDemo();

    public YopClientBenchmark(int defaultThreadCount, long defaultTotalCount) {
        super(defaultThreadCount, defaultTotalCount);
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_local.json");
//        System.setProperty("yop.sdk.config.file", "config/yop_sdk_config_dev.json");

        YopClientBenchmark benchmark = new YopClientBenchmark(DEFAULT_THREAD_COUNT, DEFAULT_TOTAL_COUNT);
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
            YopRequest request = new YopRequest();
            request.setSignAlg("SHA-256");

//            if (random.nextInt(100) >= 20) {// 20% 的请求超过30s
//                request.addParam("backendLatency", "100");
//            } else {
//                request.addParam("backendLatency", "200");
//            }

//            YopResponse response = null;
            try {
                StopWatch stopWatch = StopWatch.createStarted();
                demo.testRsa2();
                stopWatch.stop();
                if (stopWatch.getTime() > 1500) {
                    LOGGER.info("stopWatch:{}", stopWatch.getTime());
                }

//                response = YopClient.post("/rest/v1.0/yop/mock/backend-latency", request);
//                assertTrue(response.isSuccess());
//                assertTrue(response.isValidSign());
//                stopWatch.stop();
//                System.out.println("stopWatch:" + stopWatch.getTime() + "\t" + response.isValidSign());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
