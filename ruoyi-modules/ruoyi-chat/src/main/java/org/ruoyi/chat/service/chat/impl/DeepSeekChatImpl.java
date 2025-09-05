package org.ruoyi.chat.service.chat.impl;


import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.Response;
import org.ruoyi.chat.enums.ChatModeType;
import org.ruoyi.chat.service.chat.IChatService;
import org.ruoyi.chat.support.ChatServiceHelper;
import org.ruoyi.common.chat.entity.chat.Message;
import org.ruoyi.common.chat.request.ChatRequest;
import org.ruoyi.domain.vo.ChatModelVo;
import org.ruoyi.service.IChatModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * deepseek
 */
@Service
@Slf4j
public class DeepSeekChatImpl  implements IChatService {

    @Autowired
    private IChatModelService chatModelService;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // 创建一个用于直接API调用的OkHttpClient
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    @Override
    public SseEmitter chat(ChatRequest chatRequest, SseEmitter emitter) {
        ChatModelVo chatModelVo = chatModelService.selectModelByName(chatRequest.getModel());

        // 检查是否启用深度思考且是deepseek模型
        if (Boolean.TRUE.equals(chatRequest.getEnableThinking())) {
            return handleDeepSeekWithThinking(chatRequest, emitter, chatModelVo);
        }

        StreamingChatModel chatModel = OpenAiStreamingChatModel.builder()
                .baseUrl(chatModelVo.getApiHost())
                .apiKey(chatModelVo.getApiKey())
                .modelName(chatModelVo.getModelName())
                .logRequests(true)
                .logResponses(true)
                .temperature(0.8)
                .build();
        // 发送流式消息
        try {
            chatModel.chat(chatRequest.getPrompt(), new StreamingChatResponseHandler() {
                @SneakyThrows
                @Override
                public void onPartialResponse(String partialResponse) {
                    emitter.send(partialResponse);
                    log.info("收到消息片段: {}", partialResponse);
                    System.out.print(partialResponse);
                }

                @Override
                public void onCompleteResponse(ChatResponse completeResponse) {
                    emitter.complete();
                    log.info("消息结束，完整消息ID: {}", completeResponse);
                }

                @Override
                public void onError(Throwable error) {
                    System.err.println("错误: " + error.getMessage());
                    ChatServiceHelper.onStreamError(emitter, error.getMessage());
                }
            });

        } catch (Exception e) {
            log.error("deepseek请求失败：{}", e.getMessage());
            // 同步异常直接通知失败
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }

        return emitter;
    }

    /**
     * 处理启用深度思考的deepseek模型请求
     */
    private SseEmitter handleDeepSeekWithThinking(ChatRequest chatRequest, SseEmitter emitter, ChatModelVo chatModelVo) {
        try {
            // 构建请求到外部API
            String url = chatModelVo.getApiHost()+"/v1/chat/completions";
            String apiKey = chatModelVo.getApiKey();

            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", chatModelVo.getModelName());
            requestBody.put("response_format", Map.of("type", "text"));
            requestBody.put("max_tokens", 81920);
            requestBody.put("temperature", 1);
            requestBody.put("top_p", 1);
            requestBody.put("top_k", 50);
            requestBody.put("enable_thinking", chatRequest.getEnableThinking());
            requestBody.put("stream", chatRequest.getStream());

            // 构建消息
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "你是 PPIO派欧云 AI 助手，你会以诚实专业的态度帮助用户，用中文回答问题。\n"
            ));

            // 添加用户消息
            Map<String, String> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", chatRequest.getPrompt());
            messages.add(userMessage);

            requestBody.put("messages", messages);

            // 创建ObjectMapper实例
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);

            // 打印请求体用于调试
            log.info("打印请求体: {}", requestBodyStr);

            // 创建请求
            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBodyStr, JSON))
                    .build();

            // 执行异步请求
            this.client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    try {
                        log.error("深度思考请求失败: {}", e.getMessage(), e);
                        emitter.send("深度思考请求失败: " + e.getMessage());
                        emitter.complete();
                    } catch (IOException ioException) {
                        log.error("发送错误消息失败: {}", ioException.getMessage(), ioException);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        try {
                            log.error("深度思考请求失败，状态码: {}", response.code());
                            emitter.send("深度思考请求失败，状态码: " + response.code());
                            emitter.complete();
                            return;
                        } catch (IOException e) {
                            log.error("发送错误消息失败: {}", e.getMessage(), e);
                            return;
                        }
                    }

                    try (ResponseBody responseBody = response.body()) {
                        if (responseBody == null) {
                            log.error("响应体为空");
                            emitter.send("响应体为空");
                            emitter.complete();
                            return;
                        }

                        // 流式读取响应
                        processThinkingResponse(responseBody, emitter);
                    } catch (Exception e) {
                        log.error("处理响应时出错: {}", e.getMessage(), e);
                        try {
                            emitter.send("处理响应时出错: " + e.getMessage());
                            emitter.complete();
                        } catch (IOException ioException) {
                            log.error("发送错误消息失败: {}", ioException.getMessage(), ioException);
                        }
                    }
                }
            });

        } catch (Exception e) {
            log.error("处理深度思考请求时出错: {}", e.getMessage(), e);
            ChatServiceHelper.onStreamError(emitter, e.getMessage());
        }
        return emitter;
    }

    /**
     * 处理深度思考的流式响应（边解析边推送）
     */
    private void processThinkingResponse(ResponseBody responseBody, SseEmitter emitter) throws IOException {
        // 标记是否进入正式回答阶段
        boolean thinkingComplete = false;

        try (BufferedReader reader = new BufferedReader(responseBody.charStream())) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.startsWith("data: ")) {
                    continue;
                }

                String jsonData = line.substring(6).trim();
                if ("[DONE]".equals(jsonData)) {
                    break;
                }

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> chunk = mapper.readValue(jsonData, Map.class);

                    if (chunk.containsKey("choices") && chunk.get("choices") instanceof List) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) chunk.get("choices");
                        if (!choices.isEmpty()) {
                            Map<String, Object> choice = choices.get(0);
                            if (choice.containsKey("delta") && choice.get("delta") instanceof Map) {
                                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

                                // 推送思考过程
                                if (delta.containsKey("reasoning_content") && delta.get("reasoning_content") != null) {
                                    String reasoningChunk = delta.get("reasoning_content").toString();
                                    emitter.send(SseEmitter.event().data(reasoningChunk).name("thinking"));
                                    log.debug("Reasoning Chunk: {}", reasoningChunk);
                                }

                                // 推送正式回答
                                if (delta.containsKey("content") && delta.get("content") != null) {
                                    String content = delta.get("content").toString();

                                    // 第一次进入回答阶段时，加个提示头
                                    if (!thinkingComplete) {
                                        emitter.send(SseEmitter.event().data("\n\n回答内容：\n").name("answer-header"));
                                        thinkingComplete = true;
                                    }

                                    emitter.send(SseEmitter.event().data(content).name("answer"));
                                    log.debug("Answer Chunk:{}", content);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("解析JSON数据失败，忽略本行: {}", jsonData, e);
                }
            }

            emitter.complete();
            log.info("深度思考流式响应完成");
        } catch (IOException e) {
            log.error("读取响应流时出错: {}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().data("读取响应流时出错: " + e.getMessage()).name("error"));
                emitter.complete();
            } catch (IOException ioException) {
                log.error("发送错误消息失败: {}", ioException.getMessage(), ioException);
            }
        }
    }


    @Override
    public String getCategory() {
        return ChatModeType.DEEPSEEK.getCode();
    }
}