package org.pipelineframework.blocks.chat.query;

import java.util.List;
import java.util.Objects;
import org.pipelineframework.blocks.chat.ChatPromptEntry;
import org.pipelineframework.blocks.chat.ChatState;

/** Context selected after one captured Query observation. */
public record ChatSynthesisPrompt(ChatState state, List<ChatPromptEntry> context, String message) {
    public ChatSynthesisPrompt {
        state = Objects.requireNonNull(state, "state must not be null");
        context = List.copyOf(Objects.requireNonNull(context, "context must not be null"));
        message = Objects.requireNonNull(message, "message must not be null");
    }
}
