package org.pipelineframework.blocks.chat;

import java.util.Objects;
import java.util.Optional;

/** Exactly one public outcome: a provisional natural reply or an inert handoff. */
public record ChatResult(String disposition, Optional<ChatReply> reply, Optional<ChatHandoff> handoff) {
    public ChatResult {
        disposition = ChatEvidence.required(disposition, "disposition");
        reply = Objects.requireNonNull(reply, "reply must not be null");
        handoff = Objects.requireNonNull(handoff, "handoff must not be null");
        if ((!"REPLY".equals(disposition) || reply.isEmpty() || handoff.isPresent())
                && (!"HANDOFF".equals(disposition) || handoff.isEmpty() || reply.isPresent())) {
            throw new IllegalArgumentException("chat result must contain exactly its declared outcome");
        }
    }

    public static ChatResult replied(ChatReply reply) {
        return new ChatResult("REPLY", Optional.of(Objects.requireNonNull(reply)), Optional.empty());
    }

    public static ChatResult handedOff(ChatHandoff handoff) {
        return new ChatResult("HANDOFF", Optional.empty(), Optional.of(Objects.requireNonNull(handoff)));
    }
}
