package com.yeepay.g3.sdk.yop.utils;

import com.yeepay.g3.sdk.yop.encrypt.CertTypeEnum;
import com.yeepay.g3.sdk.yop.encrypt.DigitalEnvelopeDTO;
import org.junit.Before;
import org.junit.Test;

/**
 * title: <br/>
 * description: <br/>
 * Copyright: Copyright (c) 2018<br/>
 * Company: 易宝支付(YeePay)<br/>
 *
 * @author menghao.chen
 * @version 1.0.0
 * @since 2018/6/4 14:41
 */
public class DigitalEnvelopeTets {

    @Before
    public void init() {
        System.setProperty("yop.sdk.config.file", "/config/yop_sdk_config_air.json");
    }

    @Test
    public void decryptTest() {
        DigitalEnvelopeDTO digitalEnvelopeDTO = new DigitalEnvelopeDTO();
        digitalEnvelopeDTO.setCipherText("YDQRHqADV1VOnWJmoaQ7TmR2uotspKyGafT9M_CI732LWTx8nLpnqNIQLPQfOA9zVqqEuy6Xd8Qhaijttzy_2tFEBJ2e5jvJ_maHcNkzocQIuVDF1B7Z-kyesfoWQIhLUrVkCX2yZWP8LPr_5XGWYp5GmDrc7DWrXc9HseC2Jwh-10abh7sXQPMjkFpPVs-z7DGLKYP4vJF-5mM11epx2-MesU-484Pm6FJP_EBNq32NZ6cqtUlz3k2HzLd6_qJRbPIWAJSLBVomuSD1fo5SN9S8u6HL4oa0BRfPRlhpqIyG_txb3fPoRdWuZ7B1cCpFPwTyzX6y7_ihVC2ui7DAhg$tLRs0L-t_tE08uO-1nXfuN55jffgIjuo76KVDBwpYK14hQtHB7wLn8CYwH83-j6yqR8-rGDVh6qyBQV7iSuoEwiaR_KkodiRaQFtFXMnUX5SnMJ5YxfuRI5GVnuqDBVylVk8aCl77HQuKCXzINJF08CKQuZxYo_hYfh2h5Iy9imwLbPm9h0hDy0JE5JyvAhwsT_4xJKLdmA8zM_0GQ6zMT6UCP4QhGjzeVnczwQ5HpRLWpm_a255M6R70frSzOYJnaSeZEsOM31Qssx-87uvhuenf4fi9HgTn-VYiKiOoGrrcUPSKzyo_d3eM3AWuzc60HIdm3VM1fwICPKnOEAIiPV9MmtKwhEy57PtjqnAOjEJLNFP87itjCW4gpyX2kMpA2wHR2oJ8LkYM6cS8xTJC0zE_KW9j6MrRRyeeNkEArhGYS5OvmsHC3-w2OPPnHcyt4xEGrrI6d8kkXB5coQERw$AES$SHA256");

        System.out.println(DigitalEnvelopeUtils.decrypt(digitalEnvelopeDTO, InternalConfig.getISVPrivateKey(CertTypeEnum.RSA2048),
                InternalConfig.getYopPublicKey(CertTypeEnum.RSA2048)));
    }
}
