package org.pipelineframework.blocks.chat;

import java.util.Objects;

/** One captured inference with the application-authorized state carried outside model output. */
public record ChatCompletion(ChatState state, ChatModelReply reply) {
    public ChatCompletion {
        state = Objects.requireNonNull(state, "state must not be null");
        reply = Objects.requireNonNull(reply, "reply must not be null");
    }
}
