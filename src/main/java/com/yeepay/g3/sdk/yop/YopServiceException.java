package com.yeepay.g3.sdk.yop;

import com.yeepay.g3.sdk.yop.exception.YopClientException;

/**
 * title: 业务异常<br/>
 * description: 描述<br/>
 * Copyright: Copyright (c)2014<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author baitao.ji
 * @version 1.0.0
 * @since 16/2/22 09:08
 */
public class YopServiceException extends YopClientException {

    private static final long serialVersionUID = 1483785729559154396L;

    public enum ErrorType {
        Client,
        Service,
        Unknown
    }

    /**
     * 请求服务的唯一标识号
     */
    private String requestId;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 请求失败的责任方
     */
    private ErrorType errorType = ErrorType.Unknown;

    /**
     * 错误消息
     */
    private String errorMessage;

    /**
     * HTTP status code
     */
    private int statusCode;

    public YopServiceException(String errorMessage) {
        super(null);
        this.errorMessage = errorMessage;
    }

    public YopServiceException(Throwable cause, String errorMessage) {
        super(null, cause);
        this.errorMessage = errorMessage;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    @Override
    public String getMessage() {
        return this.getErrorMessage() + " (Status Code: " + this.getStatusCode() + "; Error Code: "
                + this.getErrorCode() + "; Request ID: " + this.getRequestId() + ")";
    }

}
