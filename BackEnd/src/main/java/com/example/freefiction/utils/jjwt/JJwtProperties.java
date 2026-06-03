package com.example.freefiction.utils.jjwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JJwtProperties {
    // 秘钥
    private String secret;
    // 有效时间
    private Long expiration;
    // 刷新时间
    private Long refresh;
    // 用户凭证
    private String header;

}
