package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.YopServiceException;

import java.util.concurrent.*;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 18/2/8 16:06
 */
public class Holder<V> {

    private final FutureTask<V> futureTask;

    public Holder(Callable<V> initProcess) {
        this.futureTask = new FutureTask<V>(initProcess);
    }

    public V getValue() {
        this.futureTask.run();
        try {
            return this.futureTask.get();
        } catch (InterruptedException ex) {
            throw new YopServiceException(ex, "InterruptedException occurred when get Hold Value.");
        } catch (ExecutionException ex) {
            throw new YopServiceException(ex.getCause(), "Unexpected Exception occurred when get Hold Value.");
        }
    }

    public V getValue(int timeout, TimeUnit timeUnit) {
        this.futureTask.run();
        try {
            return this.futureTask.get(timeout, timeUnit);
        } catch (InterruptedException ex) {
            throw new YopServiceException(ex, "InterruptedException occurred when get Hold Value.");
        } catch (ExecutionException ex) {
            throw new YopServiceException(ex.getCause(), "Unexpected Exception occurred when get Hold Value.");
        } catch (TimeoutException ex) {
            throw new YopServiceException(ex.getCause(), "Timeout Exception occurred when get Hold Value.");
        }
    }
}
