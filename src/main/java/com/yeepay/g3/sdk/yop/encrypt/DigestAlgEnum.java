/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

import java.util.HashMap;
import java.util.Map;

public enum DigestAlgEnum {

    /**
     * 未授权
     */
    SHA256("SHA256", "sha-256摘要"),
    SHA512("SHA512", "sha-512摘要");

    private static final Map<String, DigestAlgEnum> VALUE_MAP = new HashMap<String, DigestAlgEnum>();

    private String value;
    private String displayName;

    static {
        for (DigestAlgEnum item : DigestAlgEnum.values()) {
            VALUE_MAP.put(item.value, item);
        }
    }

    DigestAlgEnum(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static DigestAlgEnum parse(String value) {
        return VALUE_MAP.get(value);
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Map<String, DigestAlgEnum> getValueMap() {
        return VALUE_MAP;
    }
}