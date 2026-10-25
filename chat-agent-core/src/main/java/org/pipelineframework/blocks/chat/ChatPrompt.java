package org.pipelineframework.blocks.chat;

import java.util.List;
import java.util.Objects;

/** Model-visible text plus trusted state excluded from model projection. */
public record ChatPrompt(ChatState state, List<ChatPromptEntry> context, String message) {
    public ChatPrompt {
        state = Objects.requireNonNull(state, "state must not be null");
        context = List.copyOf(Objects.requireNonNull(context, "context must not be null"));
        message = Objects.requireNonNull(message, "message must not be null");
    }
}
