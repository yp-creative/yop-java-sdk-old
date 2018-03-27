package com.yeepay.g3.sdk.yop.encrypt;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * title: 数字信封dto<br>
 * description: 描述<br>
 * Copyright: Copyright (c)2014<br>
 * Company: 易宝支付(YeePay)<br>
 *
 * @author wenkang.zhang
 * @version 1.0.0
 * @since 16/11/28 上午10:19
 */
public class DigitalEnvelopeDTO implements Serializable {

    private static final long serialVersionUID = -5365630128856068164L;

    /**
     * appKey，用以确定对方公钥
     */
    private String appKey;

    /**
     * 证书类型
     */
    private CertTypeEnum certType;

    /**
     * 对称加密算法
     */
    private SymmetricEncryptAlgEnum symmetricEncryptAlg;

    /**
     * 摘要算法
     */
    private DigestAlgEnum digestAlg;

    /**
     * 明文
     */
    private String plainText;

    /**
     * 密文
     */
    private String cipherText;

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

    public SymmetricEncryptAlgEnum getSymmetricEncryptAlg() {
        return symmetricEncryptAlg;
    }

    public void setSymmetricEncryptAlg(SymmetricEncryptAlgEnum symmetricEncryptAlg) {
        this.symmetricEncryptAlg = symmetricEncryptAlg;
    }

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

    public String getCipherText() {
        return cipherText;
    }

    public void setCipherText(String cipherText) {
        this.cipherText = cipherText;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
