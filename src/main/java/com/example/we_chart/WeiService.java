package com.example.we_chart;

import com.example.we_chart.common.HttpClientUtil;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * @author lichunfeng
 */
@Service
public class WeiService {
    //Lrfun测试公众号
    private static final String app_id = "wxb0e3b2d389bd4e1e";
    private static final String app_secret = "6d60e79ec7efcdd3ee85cca1cbc6e895";

    /**
     * 获取access_tocken
     */
    public String getAccessToken() throws Exception {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + app_id + "&secret="
            + app_secret;
        Map<String, Object> accessTokenMap = HttpClientUtil.httpClientGet(url);
        return accessTokenMap.get("access_token").toString();
    }

    /**
     * 通过openid获取用户信息
     * @param openid
     * @return
     * @throws Exception
     */
    public Map<String, Object> getUserInfoByOpenid(String openid) throws Exception {
        String access_tocken = getAccessToken();
        String url = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=" + access_tocken + "&openid=" + openid;
        Map<String, Object> map = HttpClientUtil.httpClientGet(url);
        return map;
    }

}
