package com.yeepay.g3.sdk.yop.client;

/**
 * <pre>
 * 功能说明：
 * </pre>
 *
 * @author wang.bao
 * @version 1.0
 */
public class YopConstants {

	public static final String CLIENT_VERSION = "2.0.0";

	public static final String ENCODING = "UTF-8";

	public static final String SUCCESS = "SUCCESS";

	public static final String CALLBACK = "callback";

	// 方法的默认参数名
	public static final String METHOD = "method";

	// 格式化默认参数名
	public static final String FORMAT = "format";

	// 本地化默认参数名
	public static final String LOCALE = "locale";

	// 会话id默认参数名
	public static final String SESSION_ID = "sessionId";

	// 应用键的默认参数名 ;
	public static final String APP_KEY = "appKey";

	// 服务版本号的默认参数名
	public static final String VERSION = "v";

	// 签名的默认参数名
	public static final String SIGN = "sign";

	// 返回结果是否签名
	public static final String SIGN_RETURN = "signRet";

	// 商户编号
	public static final String CUSTOMER_NO = "customerNo";

	// 加密报文key
	public static final String ENCRYPT = "encrypt";

	// 时间戳
	public static final String TIMESTAMP = "ts";

	// 保护参数
	public static final String[] PROTECTED_KEY = { APP_KEY, VERSION, SIGN,
			METHOD, FORMAT, LOCALE, SESSION_ID, CUSTOMER_NO, ENCRYPT,
			SIGN_RETURN, TIMESTAMP };

	public static final String ALG_MD5 = "MD5";
	public static final String ALG_AES = "AES";
	public static final String ALG_SHA = "SHA";
	public static final String ALG_SHA1 = "SHA1";

	/**
	 * 判断是否为保护参数
	 *
	 * @param key
	 * @return
	 */
	public static boolean isProtectedKey(String key) {
		for (String k : YopConstants.PROTECTED_KEY) {
			if (k.equals(key)) {
				return true;
			}
		}
		return false;
	}
}
