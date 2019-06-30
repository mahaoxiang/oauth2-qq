package com.mhx.oauth2qq.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author MHX
 * @date 2019/6/30
 */
@RestController
public class Oauth2CallbackController {

    private static final String URL_GET_ACCESS_TOKEN = "https://graph.qq.com/oauth2.0/token";
    private static final String URL_GET_OPENID = "https://graph.qq.com/oauth2.0/me";
    private static final String URL_GET_USER_INFO = "https://graph.qq.com/user/get_user_info";

    @Value("${app.id}")
    private String appId;

    @Value("${app.secret}")
    private String appSecret;

    @Value("${oauth.callback.url}")
    private String oauthCallbackUrl;

    @GetMapping("/oauth/callback")
    public String authCallback(HttpSession session, String code, String state) throws Exception {
        System.out.println("code=" + code);
        System.out.println("state=" + state);
        String codeInSession = (String) session.getAttribute("state");
        if (!StringUtils.equals(codeInSession, state)) {
            return "参数校验失败";
        }

        // 根据code获得access_token
        String accessToken = getAccessToken(code);
        System.out.println("accessToken=" + accessToken);

        // 根据access_token获得openId
        String openId = getOpenId(accessToken);
        System.out.println("openId=" + openId);

        // 获得用户基本信息
        String userInfoStr = getUserInfo(accessToken, openId);
        return userInfoStr;
    }

    private String getAccessToken(String code) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder(URL_GET_ACCESS_TOKEN);
        builder.setParameter("grant_type", "authorization_code")
                .setParameter("client_id", appId)
                .setParameter("client_secret", appSecret)
                .setParameter("code", code)
                .setParameter("redirect_uri", oauthCallbackUrl);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(builder.build());
        HttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        List<NameValuePair> queryParams = new URIBuilder("?" + responseBody).getQueryParams();
        return queryParams
                .stream()
                .filter(pair -> "access_token" .equals(pair.getName()))
                .map(pair -> pair.getValue())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("获得access_token失败"));
    }

    private String getOpenId(String accessToken) throws URISyntaxException, IOException {
        URIBuilder builder = new URIBuilder(URL_GET_OPENID);
        builder.setParameter("access_token", accessToken);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(builder.build());
        HttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return StringUtils.substringBetween(responseBody, "\"openid\":\"", "\"");
    }

    private String getUserInfo(String accessToken, String openId) throws Exception {
        URIBuilder builder = new URIBuilder(URL_GET_USER_INFO);
        builder.setParameter("access_token", accessToken)
                .setParameter("oauth_consumer_key", appId)
                .setParameter("openid", openId);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(builder.build());
        HttpResponse response = httpClient.execute(httpGet);
        String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        return responseBody;
    }
}
