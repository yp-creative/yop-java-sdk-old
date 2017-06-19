package com.yeepay.g3.sdk.yop.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public final class ApiConfig implements Serializable {

    private static final long serialVersionUID = -6377916283927611130L;

    @JsonProperty("cfca")
    private Boolean cfca;

    public Boolean getCfca() {
        return cfca;
    }

    public void setCfca(Boolean cfca) {
        this.cfca = cfca;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this,
                ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
