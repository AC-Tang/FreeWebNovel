package com.example.freefiction.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

// DeepSeek配置类
@Component
@ConfigurationProperties(prefix = "deepseek.api")
@Data
public class DeepSeekConfig {
    private String baseUrl;
    private String key;
    private String chatEndpoint;
    private Integer timeout;
    private Integer maxTokens;
    private Double temperature;
    private String model;
}
