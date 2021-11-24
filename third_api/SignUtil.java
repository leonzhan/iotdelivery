package cn.controlsys.common;

import cn.controlsys.exception.BusinessException;
import cn.controlsys.exception.ErrorCodes;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


public class SignUtil {

    public static final String API_KEY = "apiKey";
    public static final String API_KEY_SECRET = "apiKeySecret";
    public static final String ADMIN_UUID = "adminUuid";
    public static final String SIGN = "sign";
    public static final String TIMESTAMP = "timestamp";
    private static final Logger logger = LoggerFactory.getLogger(SignUtil.class);
    public static int EXPIRE = 600;


    /**
     * generate sign data
     *
     * @param params method params
     * @return encoded sign data
     */
    public static String generateSign(SortedMap<String, String> params, String appSecret) {
        StringBuilder content = new StringBuilder();
        List<String> keys = new ArrayList<>(params.keySet());

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            if (SIGN.equals(key)) {
                continue;
            }
            String value = params.get(key).toString();
            if (value != null) {
                content.append(i == 0 ? "" : "&").append(key).append("=").append(value);
            } else {
                content.append(i == 0 ? "" : "&").append(key).append("=");
            }
        }
        String signData = content.toString();
        System.out.println("outSign: " + signData);
        String encodeSign = encodeBase64(signData, appSecret);
        System.out.println("outSign encode: " + encodeSign);
        return encodeSign;
    }

    private static String encodeBase64(String outSignData, String appSecret) {
        byte[] hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, appSecret).hmac(outSignData);
        return new String(Base64.encodeBase64(hmac));
    }

    /**
     * validate sign data
     *
     * @param params        method params
     * @param appSecret     app secret
     * @param expireSeconds expire seconds
     */
    public static void validateSign(SortedMap<String, String> params, String appSecret, int expireSeconds) throws BusinessException {

        String sign = params.get(SIGN);
        if (StringUtils.isBlank(sign)) {
            throw new BusinessException(ErrorCodes.API_CREDENTIAL_SIGN_NOT_FOUND);
        }

        String timestampStr = params.get(TIMESTAMP);
        if (StringUtils.isBlank(timestampStr)) {
            throw new BusinessException(ErrorCodes.API_CREDENTIAL_TIMESTAMP_NOT_FOUND);
        }

        long timestamp = 0;
        try {
            timestamp = Long.parseLong(timestampStr);
        } catch (Exception e) {
            logger.error("发生异常", e);
        }
        // 请求传过来的时间戳与服务器当前时间戳差值大于DURATION，则当前请求的timestamp无效
        long currentTimeMillis = System.currentTimeMillis();
        logger.info("current time millis: {}", currentTimeMillis);
        if (Math.abs(timestamp - currentTimeMillis) > expireSeconds * 1000) {
            throw new BusinessException(ErrorCodes.API_CREDENTIAL_OUT_OF_DATE);
        }

        // 根据请求传过来的参数构造签名，如果和接口的签名不一致，则请求参数被篡改
        SortedMap<String, String> signTreeMap = new TreeMap<>();
        signTreeMap.putAll(params);
        String currentSign = generateSign(signTreeMap, appSecret);
        if (!sign.equalsIgnoreCase(currentSign)) {
            throw new BusinessException(ErrorCodes.API_CREDENTIAL_SIGN_NOT_MATCH);
        }

    }

    public static void main(String[] args) {
        System.out.println("********************************* 签名 *********************************");
//        long timestamp = LocalDateTime.now().plusMinutes(-10).toInstant(ZoneOffset.ofHours(8)).toEpochMilli();
//        long timestamp = System.currentTimeMillis();
        SortedMap<String, String> map = new TreeMap<>();
        map.put("apiKey", "1");
//        map.put("k1", "k1");
//        map.put("k2", "k2");
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        String sign = SignUtil.generateSign(map, "1");
        map.put("sign", sign);
        String outParams = JSONObject.toJSONString(map);
        System.out.println("outParams: " + outParams);

        System.out.println("\n\n********************************* 验签 *********************************");
        SortedMap<String, String> inMap = JSONObject.parseObject(outParams, new TypeReference<SortedMap<String, String>>() {
        });
        // 校验请求是否过期

        System.out.println("验签结果: ");
        try {
            SignUtil.validateSign(inMap, "1", EXPIRE);
        } catch (BusinessException e) {
            e.printStackTrace();
        }
    }
}