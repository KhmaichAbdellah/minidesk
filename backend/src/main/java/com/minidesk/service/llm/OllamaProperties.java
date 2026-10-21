package com.minidesk.service.llm;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ollama")
public class OllamaProperties {

    private String endpoint = "http://127.0.0.1:11434/v1/chat/completions";

    private String model = "phi3:mini";
}