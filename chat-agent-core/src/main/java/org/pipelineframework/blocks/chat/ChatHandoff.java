package org.pipelineframework.blocks.chat;

import java.util.Objects;

/** Inert application action proposal; producing compositions must validate it before emission. */
public record ChatHandoff(ChatState state, ChatProposal proposal) {
    public ChatHandoff {
        state = Objects.requireNonNull(state, "state must not be null");
        proposal = Objects.requireNonNull(proposal, "proposal must not be null");
    }
}
