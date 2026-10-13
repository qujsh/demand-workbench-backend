package com.demandworkbench.requirement.infrastructure.ai;

import com.demandworkbench.requirement.application.PublishSummaryGenerator;
import java.nio.charset.StandardCharsets;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import reactor.core.publisher.Flux;

@Component
public class SpringAiPublishSummaryGenerator implements PublishSummaryGenerator {
    private final ChatClient chatClient;
    private final String systemPrompt;

    public SpringAiPublishSummaryGenerator(
            ChatClient.Builder builder,
            @Value("classpath:prompts/publish-summary-system.st") Resource prompt) {
        this.chatClient = builder.build();
        try {
            this.systemPrompt = StreamUtils.copyToString(prompt.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load publish summary prompt", exception);
        }
    }

    @Override
    public Flux<String> generate(String requirementText) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(user -> user.text("请生成以下需求的发布任务简介：\n\n{requirement}")
                        .param("requirement", requirementText))
                .stream()
                .content();
    }
}
