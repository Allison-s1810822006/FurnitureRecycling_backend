package edu.fcu.furniturerecyclingbackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="line")
@Getter
@Setter
public class LineProperties {
    private String channelId, channelSecret, callbackUrl, authorizeUrl, tokenUrl, jwksUrl, scope;
}

