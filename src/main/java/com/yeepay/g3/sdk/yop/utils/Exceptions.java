package com.yeepay.g3.sdk.yop.utils;

import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * title: 异常工具类<br>
 * description: @see guava的Throwables<br>
 * Copyright: Copyright (c)2011<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author dreambt
 * @version 1.0.0
 * @since 2016/11/23 下午3:34
 */
public class Exceptions {

    /**
     * 将CheckedException转换为UncheckedException.
     */
    public static RuntimeException unchecked(Throwable ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        } else {
            return new RuntimeException(ex);
        }
    }

    /**
     * 将ErrorStack转化为String
     */
    public static String getStackTraceAsString(Throwable ex) {
        StringWriter stringWriter = new StringWriter();
        ex.printStackTrace(new PrintWriter(stringWriter));
        for (StackTraceElement e : ex.getStackTrace()) {
            String className = StringUtils.trim(e.getClassName());
            if (StringUtils.startsWith(className, "at com.yeepay.g3")
                    || !StringUtils.startsWithAny(className,
                    "at org.springframework.jdbc.support",
                    "at org.springframework.transaction",
                    "at org.springframework.aop",
                    "at org.apache.ibatis",
                    "at sun.reflect",
                    "at com.ibm.db2",
                    "at java.util.concurrent",
                    "at com.sun.proxy")) {
                stringWriter.append(e.toString() + "\n");
            }
        }
        return stringWriter.toString();
    }

    /**
     * 获取组合本异常信息与底层异常信息的异常描述, 适用于本异常为统一包装异常类，底层异常才是根本原因的情况
     */
    public static String getErrorMessageWithNestedException(Throwable ex) {
        String message = ex.getMessage();
        Throwable nestedException = ex.getCause();
        if (null != nestedException) {
            message += " nested exception is " + nestedException.getClass().getName() + ":" + nestedException.getMessage();
        }
        return message;
    }

    /**
     * 获取异常的Root Cause
     */
    public static Throwable getRootCause(Throwable ex) {
        Throwable cause;
        while ((cause = ex.getCause()) != null) {
            ex = cause;
        }
        return ex;
    }

    /**
     * 判断异常是否由某些底层的异常引起.
     */
    public static boolean isCausedBy(Exception ex, Class<? extends Exception>... causeExceptionClasses) {
        Throwable cause = ex;
        while (cause != null) {
            for (Class<? extends Exception> causeClass : causeExceptionClasses) {
                if (causeClass.isInstance(cause)) {
                    return true;
                }
            }
            cause = cause.getCause();
        }
        return false;
    }
}
