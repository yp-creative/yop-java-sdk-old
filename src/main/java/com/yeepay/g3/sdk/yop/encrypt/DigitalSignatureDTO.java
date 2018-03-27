package com.yeepay.g3.sdk.yop.encrypt;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: <br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/28 下午5:36
 */
public class DigitalSignatureDTO implements Serializable {

    private static final long serialVersionUID = -5365630128856068164L;

    private String appKey;

    private CertTypeEnum certType;

    /**
     * 摘要算法
     */
    private DigestAlgEnum digestAlg;

    /**
     * 明文
     */
    private String plainText;

    /**
     * 签名
     */
    private String signature;

    public DigestAlgEnum getDigestAlg() {
        return digestAlg;
    }

    public void setDigestAlg(DigestAlgEnum digestAlg) {
        this.digestAlg = digestAlg;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public CertTypeEnum getCertType() {
        return certType;
    }

    public void setCertType(CertTypeEnum certType) {
        this.certType = certType;
    }
}
