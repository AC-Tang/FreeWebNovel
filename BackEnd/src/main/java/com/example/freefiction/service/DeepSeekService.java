//package com.example.freefiction.service;
//
//import com.example.freefiction.config.DeepSeekConfig;
//import com.example.freefiction.handler.DeepSeekRequest;
//import com.example.freefiction.handler.DeepSeekResponse;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.DeserializationFeature;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//
//import java.io.BufferedReader;
//import java.io.InputStreamReader;
//import java.util.concurrent.TimeUnit;
//import java.util.function.Consumer;
//
//// DeepSeek API客户端
//@Service
//@Slf4j
//public class DeepSeekService {
//
//    @Autowired
//    private DeepSeekConfig config;
//
//    private final OkHttpClient client;
//    private final ObjectMapper objectMapper;
//
//    public DeepSeekService(DeepSeekConfig config) {
//        this.config = config;
//        this.objectMapper = new ObjectMapper()
//                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//
//        this.client = new OkHttpClient.Builder()
//                .connectTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
//                .readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
//                .writeTimeout(config.getTimeout(), TimeUnit.MILLISECONDS)
//                .addInterceptor(new HttpLoggingInterceptor(log::info)
//                        .setLevel(HttpLoggingInterceptor.Level.BASIC))
//                .build();
//    }
//
//    // 普通聊天
//    public DeepSeekResponse chat(String userMessage) {
//        return chat(userMessage, null, false);
//    }
//
//    // 带系统提示词的聊天
//    public DeepSeekResponse chat(String userMessage, String systemPrompt) {
//        return chat(userMessage, systemPrompt, false);
//    }
//
//    // 流式响应（支持SSE）
//    public void chatStream(String userMessage, String systemPrompt,
//                           Consumer<String> chunkConsumer) {
//        try {
//            DeepSeekRequest request = buildRequest(userMessage, systemPrompt, true);
//
//            Request httpRequest = new Request.Builder()
//                    .url(config.getBaseUrl() + config.getChatEndpoint())
//                    .post(createRequestBody(request))
//                    .addHeader("Authorization", "Bearer " + config.getKey())
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//
//            try (Response response = client.newCall(httpRequest).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("API调用失败: " + response.code());
//                }
//
//                // 处理流式响应
//                try (BufferedReader reader = new BufferedReader(
//                        new InputStreamReader(response.body().byteStream()))) {
//
//                    String line;
//                    while ((line = reader.readLine()) != null) {
//                        if (line.startsWith("data: ")) {
//                            String data = line.substring(6);
//                            if (data.equals("[DONE]")) {
//                                break;
//                            }
//                            if (!data.trim().isEmpty()) {
//                                chunkConsumer.accept(data);
//                            }
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("流式聊天失败", e);
//            throw new RuntimeException("流式聊天失败: " + e.getMessage());
//        }
//    }
//
//    // 普通请求
//    public DeepSeekResponse chat(String userMessage, String systemPrompt,
//                                 boolean stream) {
//        try {
//            DeepSeekRequest request = buildRequest(userMessage, systemPrompt, stream);
//
//            Request httpRequest = new Request.Builder()
//                    .url(config.getBaseUrl() + config.getChatEndpoint())
//                    .post(createRequestBody(request))
//                    .addHeader("Authorization", "Bearer " + config.getKey())
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//
//            try (Response response = client.newCall(httpRequest).execute()) {
//                if (!response.isSuccessful()) {
//                    String errorBody = response.body() != null ?
//                            response.body().string() : "无错误信息";
//                    log.error("DeepSeek API调用失败: {} - {}",
//                            response.code(), errorBody);
//                    throw new RuntimeException("API调用失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                return objectMapper.readValue(responseBody, DeepSeekResponse.class);
//            }
//        } catch (Exception e) {
//            log.error("DeepSeek API调用异常", e);
//            throw new RuntimeException("AI服务暂时不可用");
//        }
//    }
//
//    // 带历史记录的聊天
//    public DeepSeekResponse chatWithHistory(List<DeepSeekRequest.Message> history) {
//        try {
//            DeepSeekRequest request = DeepSeekRequest.builder()
//                    .model(config.getModel())
//                    .messages(history)
//                    .temperature(config.getTemperature())
//                    .maxTokens(config.getMaxTokens())
//                    .stream(false)
//                    .build();
//
//            Request httpRequest = new Request.Builder()
//                    .url(config.getBaseUrl() + config.getChatEndpoint())
//                    .post(createRequestBody(request))
//                    .addHeader("Authorization", "Bearer " + config.getKey())
//                    .addHeader("Content-Type", "application/json")
//                    .build();
//
//            try (Response response = client.newCall(httpRequest).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new RuntimeException("API调用失败: " + response.code());
//                }
//
//                String responseBody = response.body().string();
//                return objectMapper.readValue(responseBody, DeepSeekResponse.class);
//            }
//        } catch (Exception e) {
//            log.error("带历史聊天失败", e);
//            throw new RuntimeException("聊天失败");
//        }
//    }
//
//    // 构建请求
//    private DeepSeekRequest buildRequest(String userMessage, String systemPrompt,
//                                         boolean stream) {
//        List<DeepSeekRequest.Message> messages = new ArrayList<>();
//
//        if (systemPrompt != null && !systemPrompt.trim().isEmpty()) {
//            messages.add(DeepSeekRequest.Message.builder()
//                    .role("system")
//                    .content(systemPrompt)
//                    .build());
//        }
//
//        messages.add(DeepSeekRequest.Message.builder()
//                .role("user")
//                .content(userMessage)
//                .build());
//
//        return DeepSeekRequest.builder()
//                .model(config.getModel())
//                .messages(messages)
//                .temperature(config.getTemperature())
//                .maxTokens(config.getMaxTokens())
//                .stream(stream)
//                .build();
//    }
//
//    // 创建请求体
//    private RequestBody createRequestBody(DeepSeekRequest request) {
//        try {
//            String json = objectMapper.writeValueAsString(request);
//            return RequestBody.create(json, MediaType.get("application/json"));
//        } catch (JsonProcessingException e) {
//            throw new RuntimeException("请求体创建失败", e);
//        }
//    }
//
//    // 估算token数量（简单版本）
//    public int estimateTokens(String text) {
//        // 简单估算：英文约4字符1个token，中文约1.5字符1个token
//        int chineseChars = (int) text.chars()
//                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
//                .count();
//        int otherChars = text.length() - chineseChars;
//
//        return (int) (chineseChars / 1.5 + otherChars / 4.0);
//    }
//}
