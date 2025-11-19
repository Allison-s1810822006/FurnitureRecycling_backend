package edu.fcu.furniturerecyclingbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * WebConfig
 * 全域 Spring Web 設定類別，主要用於設定 CORS（跨來源資源共享）。
 */
@Configuration // 標註為 Spring 設定類別
public class WebConfig {

    /**
     * 註冊 CORS 設定 Bean，限制到 /api/** 並允許前端 origin
     * @return WebMvcConfigurer 實例
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        // 使用匿名類別覆寫 addCorsMappings 方法
        return new WebMvcConfigurer() {
            /**
             * 設定 CORS 規則：
             * - 僅針對 /api/** 路徑
             * - 允許指定前端 origin（開發機）
             * - 允許 GET、POST、PUT、DELETE、OPTIONS
             * - 允許所有 headers，允許 credentials
             */
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                "http://localhost:5173", // optional dev frontend
                                "http://localhost:5180"  // your frontend
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("Content-Type", "Authorization", "Accept", "X-Requested-With")
                        .allowCredentials(true);
            }
        };
    }

    /**
     * Provide an explicit CorsConfigurationSource so Spring Security's cors() picks up the same config.
     * This ensures /api/** endpoints allow the required origin/methods/headers and credentials.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:5180", "http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization", "Accept", "X-Requested-With"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

}
