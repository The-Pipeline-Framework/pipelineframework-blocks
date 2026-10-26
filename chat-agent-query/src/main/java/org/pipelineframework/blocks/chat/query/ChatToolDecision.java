package org.pipelineframework.blocks.chat.query;

import org.pipelineframework.blocks.chat.ChatCompletion;

public sealed interface ChatToolDecision permits ChatToolDecision.Call, ChatToolDecision.Handoff,
    ChatToolDecision.Complete {
    String discriminator();

    record Call(AgentCall value) implements ChatToolDecision {
        @Override public String discriminator() { return "call"; }
    }

    record Handoff(HandoffCall value) implements ChatToolDecision {
        @Override public String discriminator() { return "handoff"; }
    }

    record Complete(ChatCompletion value) implements ChatToolDecision {
        @Override public String discriminator() { return "complete"; }
    }
}
