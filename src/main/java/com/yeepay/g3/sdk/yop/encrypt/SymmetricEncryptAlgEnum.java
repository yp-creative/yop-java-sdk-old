/**
 * Copyright: Copyright (c)2011
 * Company: 易宝支付(YeePay)
 */
package com.yeepay.g3.sdk.yop.encrypt;

import java.util.HashMap;
import java.util.Map;

public enum SymmetricEncryptAlgEnum {

    AES("AES", "aes加密算法");

    private static final Map<String, SymmetricEncryptAlgEnum> VALUE_MAP = new HashMap<String, SymmetricEncryptAlgEnum>();

    private String value;
    private String displayName;

    static {
        for (SymmetricEncryptAlgEnum item : SymmetricEncryptAlgEnum.values()) {
            VALUE_MAP.put(item.value, item);
        }
    }

    SymmetricEncryptAlgEnum(String value, String displayName) {
        this.value = value;
        this.displayName = displayName;
    }

    public static SymmetricEncryptAlgEnum parse(String value) {
        return VALUE_MAP.get(value);
    }

    public String getValue() {
        return value;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Map<String, SymmetricEncryptAlgEnum> getValueMap() {
        return VALUE_MAP;
    }


}