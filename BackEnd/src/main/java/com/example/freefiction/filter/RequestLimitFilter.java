package com.example.freefiction.filter;

import com.example.freefiction.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RequestLimitFilter implements HandlerInterceptor {
    private final RedisService redisService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取请求的 URI
        String requestURI = request.getRequestURI();
        // 指定需要限制的 API 路径
        String limitedApiPath = "/api/limited";

        // 判断是否是需要限制的 API
        if (limitedApiPath.equals(requestURI)) {
            String ip = request.getRemoteAddr();
            String key = "request_count:" + ip + ":" + LocalDate.now();
            Integer count = redisService.get(key, Integer.class);
            if (count == null) {
                redisService.set(key, 1, 1, TimeUnit.DAYS);
            } else if (count < 100) { // 假设每天限制 100 次请求
                redisService.increment(key);
            } else {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests. Please try again later.");
                return false;
            }
        }
        return true;
    }
}
