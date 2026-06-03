package com.example.freefiction.config;

import com.example.freefiction.filter.RequestLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    private final RequestLimitFilter requestLimitFilter;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry reg) {
        // 映射前端请求的路径到实际的文件路径
        reg.addResourceHandler("/Images/**")
                .addResourceLocations("file:D:/JavaProjectSpace/FreeFiction/Uploads/Fiction/Images/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLimitFilter);
    }
}
