package org.pipelineframework.blocks.chat;

import java.util.Objects;

/** Proposed answer and next state; the application decides whether to disclose and accept them. */
public record ChatReply(String messageId, ChatState proposedNextState, String text) {
    public ChatReply {
        messageId = ChatEvidence.required(messageId, "messageId");
        proposedNextState = Objects.requireNonNull(proposedNextState, "proposedNextState must not be null");
        text = Objects.requireNonNull(text, "text must not be null");
    }
}
