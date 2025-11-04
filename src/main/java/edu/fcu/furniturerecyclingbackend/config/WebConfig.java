package edu.fcu.furniturerecyclingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 * 全域 Spring Web 設定類別，主要用於設定 CORS（跨來源資源共享）。
 */
@Configuration // 標註為 Spring 設定類別
public class WebConfig {

    /**
     * 註冊 CORS 設定 Bean，允許所有來源與常用 HTTP 方法。
     * @return WebMvcConfigurer 實例
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        // 使用匿名類別覆寫 addCorsMappings 方法
        return new WebMvcConfigurer() {
            /**
             * 設定 CORS 規則：
             * - 允許所有路徑（/**）
             * - 允許所有來源（*）
             * - 允許 GET、POST、PUT、DELETE 方法
             */
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
