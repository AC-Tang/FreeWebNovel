package com.example.freefiction.controller;

import com.example.freefiction.domain.AuthResponse;
import com.example.freefiction.domain.Captcha;
import com.example.freefiction.entity.Users;
import com.example.freefiction.handler.Result;
import com.example.freefiction.service.sys.UsersService;
import com.example.freefiction.utils.constant.TokenBlacklist;
import com.example.freefiction.utils.jjwt.JJwtUtil;
import com.google.code.kaptcha.Producer;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class CaptchaController {
    private final Producer kaptchaProducer;
    private final TokenBlacklist tokenBlacklist;
    private final JJwtUtil jwtUtils;
    private final UsersService usersService;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;


    private static final String FROM = "2129536594@qq.com"; // 发信人（同 YML）

    /**
     * 获取验证码
     */
    @GetMapping("/captcha")
    public void getCaptcha(HttpServletResponse response) throws IOException {
        String text = kaptchaProducer.createText();
        BufferedImage image = kaptchaProducer.createImage(text);

        String captchaId = UUID.randomUUID().toString();

        // 将验证码文本存储到 Redis 中，设置过期时间为 5 分钟
        redisTemplate.opsForValue().set("captcha:" + captchaId, text, 5 * 60);

        response.setContentType("image/jpeg");
        response.setHeader("X-Captcha-Id", captchaId);
        ImageIO.write(image, "jpg", response.getOutputStream());
    }

    /**
     * 验证验证码
     */
    @PostMapping("/verify")
    public Result verifyCaptcha(@RequestBody Captcha captcha) {
        Result result = new Result<>();
        String redisKey = "captcha:" + captcha.getCaptchaId();
        if (!redisTemplate.hasKey(redisKey)) {
            result.setCode(400);
            result.setMessage("验证码已过期或无效");
            return result;
        }
        String storedCaptchaText = redisTemplate.opsForValue().get(redisKey);
        if (storedCaptchaText == null) {
            result.setCode(400);
            result.setMessage("验证码已过期或无效");
            return result;
        }
        if (!storedCaptchaText.trim().equalsIgnoreCase(captcha.getCaptcha())) {
            result.setCode(400);
            result.setMessage("验证码错误");
            return result;
        }
        redisTemplate.delete(redisKey);
        result.setCode(200);
        result.setMessage("验证码正确");
        return result;
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh_token")
    public Result refreshToken(String refreshToken) {
        Result result = new Result<>();
        if (tokenBlacklist.isBlacklisted(refreshToken)) {
            return Result.unauthorized("Refresh token is blacklisted");
        }

        try {
            Claims claims = jwtUtils.parseClaims(refreshToken);
            Integer userId =jwtUtils.getUserFromToken("userid");
            Users user = usersService.getById(userId);
            if(user== null){
                return Result.unauthorized("用户不存在");
            }

            String newAccessToken = jwtUtils.generateToken(user);
            String newRefreshToken = jwtUtils.generateRefreshToken( user);
            result.setToken(new AuthResponse(newRefreshToken, newAccessToken));

            tokenBlacklist.blacklistToken(refreshToken);
            result.setCode(200);
            result.setMessage("Token refreshed successfully");
            return result;
        } catch (Exception e) {
            return Result.unauthorized("Invalid refresh token");
        }
    }

    /**
     * 发送邮件验证码
     */
    @PostMapping("/send-email")
    public Result sendEmail(@RequestBody Captcha captcha) {
        String email = captcha.getEmail();
        Result result = new Result<>();
        if(email.isEmpty()){
            return Result.failed("邮箱不能为空");
        }

        // 创建验证码
        String captchaCode = UUID.randomUUID().toString().substring(0, 6);
        // 发送邮件
        // ...
        // 存储验证码到 Redis 中，设置过期时间为 5 分钟
        redisTemplate.opsForValue().set("captcha:email" + email, captchaCode, Duration.ofMinutes(5));

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(FROM);
        msg.setTo(email);
        msg.setSubject("注册验证码");
        msg.setText("您的注册验证码是：" + captchaCode + "\n请勿将此验证码告知他人。\n\n" + "，5分钟内有效。");
        mailSender.send(msg);

        result.setCode(200);
        result.setMessage("验证码已发送至邮箱");
        return result;
    }

    /**
     * 验证邮件验证码
     */
    @PostMapping("/verify-email")
    public Result verifyEmail(@RequestBody Captcha captcha) {
        String email = captcha.getEmail();
        Result result = new Result<>();
        String redisKey = "captcha:email" + email;
        if (!redisTemplate.hasKey(redisKey)) {
            result.setCode(400);
            result.setMessage("验证码已过期或无效");
            return result;
        }
        String storedCaptcha = redisTemplate.opsForValue().get(redisKey);
        if (storedCaptcha != null && !storedCaptcha.equals(captcha.getCode())) {
            result.setCode(400);
            result.setMessage("验证码错误");
            return result;
        }
        redisTemplate.delete(redisKey);
        result.setCode(200);
        result.setMessage("验证成功");
        return result;
    }
}
