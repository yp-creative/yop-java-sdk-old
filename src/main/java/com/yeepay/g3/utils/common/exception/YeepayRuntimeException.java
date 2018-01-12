package com.yeepay.g3.utils.common.exception;

import org.apache.commons.lang3.ArrayUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * title: <br>
 * description:描述<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2018/1/10 下午12:01
 */
public class YeepayRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 2381136802566762335L;

    /**
     * 异常ID，每个异常示例一个
     */
    private String id;

    /**
     * 异常信息，包含必要的上下文业务信息，用于打印日志
     */
    private String message;

    protected String realClassName;

    public YeepayRuntimeException() {
        super();
        initId();
    }

    /**
     * 构造器，用于系统内部主动抛出YeepayRuntimeException时使用
     *
     * @param message 异常信息或者信息模板
     * @param args    模板参数信息
     */
    public YeepayRuntimeException(String message, Object... args) {
        super();
        setMessageFormat(message, args);
        initId();
    }

    /**
     * 把捕获到的系统异常转换为YeepayRuntimeException
     *
     * @param throwable
     */
    public YeepayRuntimeException(Throwable throwable) {
        super();
        this.setMessage(throwable.getMessage());
        this.setStackTrace(throwable.getStackTrace());
        this.realClassName = throwable.getClass().getName();
        initId();
    }

    /**
     * 把捕获到的系统异常转换为YeepayRuntimeException
     *
     * @param message
     * @param throwable
     * @param args
     */
    public YeepayRuntimeException(String message, Throwable throwable,
                                  Object... args) {
        super();
        setMessageFormat(message, args);
        this.setStackTrace(throwable.getStackTrace());
        this.realClassName = throwable.getClass().getName();
        initId();
    }

    private void initId() {
        this.id = UUID.randomUUID().toString().toUpperCase().replaceAll("-", "");
    }

    public String getId() {
        return id;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    private void setMessageFormat(String message, Object... args) {
        if (message != null && args != null) {
            this.setMessage(MessageFormat.format(message, args));
        } else {
            this.setMessage(message);
        }
    }

    public String getRealClassName() {
        if (realClassName == null) {
            return this.getClass().getName();
        }
        return realClassName;
    }

    public void setRealClassName(String realClassName) {
        this.realClassName = realClassName;
    }

    public void mergeStackTrace(StackTraceElement[] stackTrace) {
        this.setStackTrace(ArrayUtils.addAll(this.getStackTrace(), stackTrace));
    }

    public StackTraceElement[] getCoreStackTrace() {
        List<StackTraceElement> list = new ArrayList<StackTraceElement>();
        for (StackTraceElement traceEle : getStackTrace()) {
            if (traceEle.getClassName().startsWith("com.yeepay")) {
                list.add(traceEle);
            }
        }
        StackTraceElement[] stackTrace = new StackTraceElement[list.size()];
        return list.toArray(stackTrace);
    }

    public String getCoreStackTraceStr() {
        StringBuffer sb = new StringBuffer();
        for (StackTraceElement traceEle : getCoreStackTrace()) {
            sb.append("\n" + traceEle.toString());
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        YeepayRuntimeException y = new YeepayRuntimeException();
        y.mergeStackTrace(Thread.currentThread().getStackTrace());
        throw y;
    }
}
