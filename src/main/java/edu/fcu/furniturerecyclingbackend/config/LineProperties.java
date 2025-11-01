package edu.fcu.furniturerecyclingbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LineProperties
 * 讀取 application.properties 內 line.* 相關設定，
 * 用於 LINE OAuth2/登入流程的參數注入。
 */
@Configuration // 標註為 Spring 設定類別，讓 Spring Boot 自動管理
@ConfigurationProperties(prefix="line") // 讀取 line.* 前綴的設定
@Getter // 自動產生 getter 方法
@Setter // 自動產生 setter 方法
public class LineProperties {
    // LINE Channel ID
    private String channelId;
    // LINE Channel Secret
    private String channelSecret;
    // LINE OAuth2 callback URL
    private String callbackUrl;
    // LINE 授權頁面 URL
    private String authorizeUrl;
    // LINE token 交換 URL
    private String tokenUrl;
    // LINE JWKs 公鑰 URL
    private String jwksUrl;
    // LINE scope 權限範圍
    private String scope;
}
