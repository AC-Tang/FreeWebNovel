package com.example.freefiction.utils.jjwt;

import com.example.freefiction.entity.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JJwtUtil {
    Logger log = LoggerFactory.getLogger(getClass());
    private static final long REFRESH_THRESHOLD = 10 * 60 * 1000L;  // 剩余10分钟自动续期
    final JJwtProperties jwtProperties;

    /**
     * 生成token
     * @param user 用户信息
     * @return  token
     */
    public String  generateToken(Users user){
        // 生成令牌ID
        String uuid = UUID.randomUUID().toString();

        SecureDigestAlgorithm<SecretKey,SecretKey> algorithm= Jwts.SIG.HS256;
        long expMillis=System.currentTimeMillis()+jwtProperties.getExpiration();
        Date exp=new Date(expMillis);

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        //令牌ID
        //过期时间
        //签发时间
        return Jwts.builder()
                .id(uuid)  //令牌ID
                .claim("userid",user.getId())
                .claim("role",user.getRoleName())
                .signWith(key,algorithm)
                .expiration(exp)  //过期时间
                .issuedAt(new Date())  //签发时间
                .compact();
    }

    /**
     * 生成刷新令牌
     * @param user 用户信息
     * @return 刷新令牌
     */
    public String generateRefreshToken(Users user){
        // 生成令牌ID
        String uuid = UUID.randomUUID().toString();

        SecureDigestAlgorithm<SecretKey,SecretKey> algorithm = Jwts.SIG.HS256;
        // 使用更长的过期时间，通常是普通令牌的数倍
        long expMillis = System.currentTimeMillis() + jwtProperties.getRefresh(); // 假设有这个配置
        Date exp = new Date(expMillis);

        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        // 令牌ID
        // 过期时间
        // 签发时间
        return Jwts.builder()
                .id(uuid)  // 令牌ID
                .claim("userid", user.getId())
                .claim("role", user.getRoleName())
                .claim("tokenType", "refresh") // 标识为刷新令牌
                .signWith(key, algorithm)
                .expiration(exp)  // 过期时间
                .issuedAt(new Date())  // 签发时间
                .compact();
    }


    /**
     * token验证
     * @param token  token
     * @return true/false
     */
    public Boolean verify(String token){
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        try{
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);

        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * 获取payload中的用户信息
     *
     * @param token JWT Token
     * @return 用户信息
     */
    public Integer getUserFromToken(String token) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            log.warn("Failed to parse claims from token");
            return null;
        }
        Integer userId = (Integer) claims.get("userid");
        if (userId == null){
            log.warn("用户ID为空");
            return null;
        }
        return userId;
    }

    /**
     * 解析JWT Token中的Claims
     *
     * @param token JWT Token
     * @return Claims
     */
    public Claims parseClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));

        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean shouldRefresh(Date expiration) {
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return remaining < REFRESH_THRESHOLD;
    }

}
