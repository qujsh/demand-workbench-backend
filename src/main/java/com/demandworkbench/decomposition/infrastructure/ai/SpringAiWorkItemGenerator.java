package com.demandworkbench.decomposition.infrastructure.ai;

import com.demandworkbench.decomposition.application.DecompositionException;
import com.demandworkbench.decomposition.application.DecompositionErrorCode;
import com.demandworkbench.decomposition.application.WorkItemGenerator;
import com.demandworkbench.decomposition.domain.model.WorkItemNode;
import com.demandworkbench.decomposition.domain.model.WorkItemTree;
import com.demandworkbench.decomposition.domain.type.Priority;
import com.demandworkbench.decomposition.domain.type.WorkItemType;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;

@Component
public class SpringAiWorkItemGenerator implements WorkItemGenerator {
    private static final int MAX_DEPTH = WorkItemType.values().length;

    private final ChatClient chatClient;
    private final String systemPrompt;

    public SpringAiWorkItemGenerator(ChatClient.Builder builder,
                                     @Value("classpath:prompts/work-item-decomposition-system.st") Resource prompt) {
        this.chatClient = builder.build();
        try {
            this.systemPrompt = StreamUtils.copyToString(prompt.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception exception) {
            throw new IllegalStateException("Cannot load decomposition prompt", exception);
        }
    }

    @Override
    public GenerationResult generate(String requirementText) {
        try {
            var aiTree = chatClient.prompt()
                    .system(systemPrompt)
                    .user(user -> user.text("请拆解以下需求：\n\n{requirement}")
                            .param("requirement", requirementText))
                    .call()
                    .entity(AiWorkItemTree.class);
            return new GenerationResult(normalize(aiTree), null, null, null);
        } catch (DecompositionException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new DecompositionException(
                    DecompositionErrorCode.INVALID_MODEL_OUTPUT, "模型返回了无效的枚举值", exception);
        } catch (Exception exception) {
            throw new DecompositionException(
                    DecompositionErrorCode.AI_REQUEST_FAILED, "AI 服务请求或响应解析失败", exception);
        }
    }

    private WorkItemTree normalize(AiWorkItemTree source) {
        if (source == null || source.modules() == null || source.modules().isEmpty()) {
            throw new DecompositionException(DecompositionErrorCode.INVALID_MODEL_OUTPUT, "模型没有返回模块数据");
        }
        var counters = new int[MAX_DEPTH];
        var modules = new ArrayList<WorkItemNode>();
        for (var module : source.modules()) {
            modules.add(normalizeNode(module, 0, counters));
        }
        return new WorkItemTree(modules);
    }

    private WorkItemNode normalizeNode(AiWorkItemTree.AiNode source, int level, int[] counters) {
        if (level >= MAX_DEPTH) {
            throw new DecompositionException(
                    DecompositionErrorCode.INVALID_MODEL_OUTPUT, "模型返回的工作项层级超过四级");
        }
        if (source == null || !StringUtils.hasText(source.name())) {
            throw new DecompositionException(DecompositionErrorCode.INVALID_MODEL_OUTPUT, "模型返回了空名称节点");
        }
        var type = WorkItemType.atLevel(level);
        var id = type.name() + "-" + String.format("%03d", ++counters[level]);
        var children = new ArrayList<WorkItemNode>();
        for (var child : safeChildren(source.children())) {
            children.add(normalizeNode(child, level + 1, counters));
        }
        return new WorkItemNode(id, type, source.name().trim(), blank(source.description()), "未开始",
                "未分配", Priority.normalize(source.priority()), null, null, blank(source.dependency()), children);
    }

    private List<AiWorkItemTree.AiNode> safeChildren(List<AiWorkItemTree.AiNode> children) {
        return children == null ? List.of() : children;
    }

    private String blank(String value) {
        return value == null ? "" : value.trim();
    }
}
