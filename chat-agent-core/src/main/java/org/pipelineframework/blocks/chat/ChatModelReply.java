package org.pipelineframework.blocks.chat;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** The only model-authored field in the direct completion projection. */
public record ChatModelReply(String text) {
    public ChatModelReply {
        text = Objects.requireNonNull(text, "text must not be null").strip();
        if (text.isEmpty() || text.getBytes(StandardCharsets.UTF_8).length > ChatState.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("model reply must contain 1 to 16384 UTF-8 bytes");
        }
    }
}
