package com.demandworkbench.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "deepseek")
public record DeepSeekProperties(String model, String promptVersion) {}
