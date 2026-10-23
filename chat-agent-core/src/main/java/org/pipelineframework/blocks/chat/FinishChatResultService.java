package org.pipelineframework.blocks.chat;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.pipelineframework.service.ReactiveService;

@ApplicationScoped
public final class FinishChatResultService implements ReactiveService<ChatResult, ChatResult> {
    @Override
    public Uni<ChatResult> process(ChatResult result) {
        return Uni.createFrom().item(result);
    }
}
