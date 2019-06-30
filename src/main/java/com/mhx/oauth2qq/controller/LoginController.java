package com.mhx.oauth2qq.controller;

import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;
import java.net.URISyntaxException;
import java.util.UUID;

/**
 * @author MHX
 * @date 2019/6/30
 */
@Controller
public class LoginController {
    private static final String URL_AUTHORIZE = "https://graph.qq.com/oauth2.0/authorize";

    @Value("${app.id}")
    private String appId;
    @Value("${oauth.callback.url}")
    private String oauthCallbackUrl;

    @GetMapping("/login/qq")
    public String loginQQ(HttpSession session) throws URISyntaxException {
        String state = UUID.randomUUID().toString();
        System.out.println("sessionId=" + session.getId());
        System.out.println("state=" + state);
        session.setAttribute("state", state);
        URIBuilder builder = new URIBuilder(URL_AUTHORIZE);
        builder.setParameter("response_type", "code")
                .setParameter("client_id", appId)
                .setParameter("redirect_uri", oauthCallbackUrl)
                .setParameter("state", state);
        return "redirect:" + builder.toString();
    }
}
