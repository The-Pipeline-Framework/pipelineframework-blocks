package org.pipelineframework.blocks.chat.query;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Objects;
import org.pipelineframework.blocks.chat.ChatHandoff;
import org.pipelineframework.blocks.chat.ChatProposal;
import org.pipelineframework.blocks.chat.ChatResult;
import org.pipelineframework.blocks.chat.ChatState;
import org.pipelineframework.config.pipeline.PipelineJson;
import org.pipelineframework.service.ReactiveService;

/** Carries a compiler-bound, canonically validated Command proposal without dispatching it. */
@ApplicationScoped
public final class ProposeChatHandoffService implements ReactiveService<HandoffCall, ChatResult> {
    @Override
    public Uni<ChatResult> process(HandoffCall call) {
        return Uni.createFrom().item(() -> propose(call));
    }

    static ChatResult propose(HandoffCall call) {
        Objects.requireNonNull(call, "handoff call must not be null");
        ChatState state;
        try {
            state = PipelineJson.mapper().readValue(call.contextJson(), TrustedContext.class).state();
        } catch (Exception failure) {
            throw new IllegalArgumentException("handoff has invalid carried chat state", failure);
        }
        return ChatResult.handedOff(new ChatHandoff(state,
            new ChatProposal(call.binding(), call.operation(), call.argumentsJson(), "COMMAND")));
    }

    private record TrustedContext(ChatState state) {
        private TrustedContext {
            state = Objects.requireNonNull(state, "carried chat state must not be null");
        }
    }
}
