package org.pipelineframework.blocks.chat;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import org.pipelineframework.service.ReactiveService;

@ApplicationScoped
public final class CompleteChatTurnService implements ReactiveService<ChatCompletion, ChatResult> {
    @Override
    public Uni<ChatResult> process(ChatCompletion completion) {
        return Uni.createFrom().item(() -> ChatResult.replied(new ChatReply(
            completion.state().messageId(), completion.state().proposedWithReply(completion.reply().text()),
            completion.reply().text())));
    }
}
