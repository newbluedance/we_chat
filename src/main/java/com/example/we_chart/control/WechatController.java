package com.example.we_chart.control;

import com.example.we_chart.WeiService;
import com.example.we_chart.common.HttpClientUtil;
import com.example.we_chart.entity.WeiImgResult;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author lichunfeng
 */
@Controller
public class WechatController {

    @Resource
    private WeiService weiService;

    /**
     * 获取access_tocken
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/wechat/getToken")
    public @ResponseBody
    String getToken() throws Exception {
        //step 1获取token
        String access_token = weiService.getAccessToken();
        return access_token;
    }

    /**
     * 生成带参数的二维码，扫描关注微信公众号，自动登录网站
     *
     * @param accessToken
     * @return img对象 可以 通过imgUrl 显示二维码
     * @throws Exception
     */
    @RequestMapping(value = "/wechat/getWeiImg")
    public @ResponseBody
    WeiImgResult getWeiImg(@RequestBody String accessToken) throws Exception {
        //step 2 获取用于生成二维码的ticket
        String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token=" + accessToken;
        String scene_str = "lcfsxg.com." + LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
        String params =
            "{\"expire_seconds\":600, \"action_name\":\"QR_STR_SCENE\", \"action_info\":{\"scene\":{\"scene_str\":\""
                + scene_str + "\"}}}";
        Map<String, Object> resultMap = HttpClientUtil.httpClientPost(url, params);
        String qrcodeUrl = null;
        if (resultMap.get("ticket") != null) {
            qrcodeUrl = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=" + resultMap.get("ticket");
        }
        return new WeiImgResult(scene_str, qrcodeUrl);
    }

    /**
     * 这是回调,当用户扫码,发送消息等时微信服务器会推送消息到此
     * 根据微信推送的消息 做出相应的处理
     * @param request
     * @return
     */
    @RequestMapping("/wechat/validUrl")
    public void validUrl(HttpServletRequest request, HttpServletResponse response) {

        /* //首次接入,填写URL时需要验证 ,之后不需要了
        SignUtil.urlCheck(request, response);*/

        //获取回调信息,转成map
        Map<String, String> callbackMap = xmlToMap(request);
        System.out.println(callbackMap);

        if (callbackMap != null && callbackMap.get("FromUserName").toString() != null) {
            // 通过openid获取用户信息
            try {
                Map<String, Object> wechatUserInfoMap = weiService.getUserInfoByOpenid(callbackMap.get("FromUserName"));
                System.out.println(wechatUserInfoMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 将数据写入到数据库中，前面自定义的参数（scene_str）也需记录到数据库中，后面用于检测匹配登录
            // INSERT INTO wechat_user_info......(数据库操作)
        }

    }

    /**
     * xml转为map
     *
     * @param httpServletRequest
     * @return
     */
    private Map<String, String> xmlToMap(HttpServletRequest httpServletRequest) {
        Map<String, String> map = new HashMap<String, String>();
        try {
            InputStream inputStream = httpServletRequest.getInputStream();
            // 读取输入流
            SAXReader reader = new SAXReader();
            org.dom4j.Document document = reader.read(inputStream);
            // 得到xml根元素
            Element root = document.getRootElement();
            // 得到根元素的所有子节点
            List<Element> elementList = root.elements();
            // 遍历所有子节点
            for (Element e : elementList) {
                map.put(e.getName(), e.getText());
            }
            // 释放资源
            inputStream.close();
            return map;
        } catch (Exception e) {
            e.getMessage();
        }
        return null;
    }
}

