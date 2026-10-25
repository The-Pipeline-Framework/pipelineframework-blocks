package org.pipelineframework.blocks.chat;

/** Explicit budget for one model inference; independent of the carried-state bound. */
public record ChatWindow(int contextLimitTokens, int instructionTokens, int reservedOutputTokens, int maxEntryBytes) {
    public static final ChatWindow DEFAULT = new ChatWindow(8_192, 512, 1_024, 4_096);

    public ChatWindow {
        if (contextLimitTokens < 1 || instructionTokens < 0 || reservedOutputTokens < 0
                || maxEntryBytes < 1 || maxEntryBytes > ChatState.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("invalid chat model window");
        }
        if ((long) instructionTokens + reservedOutputTokens >= contextLimitTokens) {
            throw new IllegalArgumentException("fixed chat overhead leaves no model input window");
        }
    }
}
