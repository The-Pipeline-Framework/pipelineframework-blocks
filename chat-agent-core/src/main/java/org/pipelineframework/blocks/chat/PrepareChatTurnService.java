package org.pipelineframework.blocks.chat;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.pipelineframework.connector.conversation.ContextAssembler;
import org.pipelineframework.connector.conversation.ContextAssemblyRequest;
import org.pipelineframework.connector.conversation.ContextCandidate;
import org.pipelineframework.service.ReactiveService;

/** Pure context selection. It neither hydrates conversation history nor calls a model. */
@ApplicationScoped
public final class PrepareChatTurnService implements ReactiveService<ChatState, ChatPrompt> {
    private final ContextAssembler assembler = new ContextAssembler();

    @Override
    public Uni<ChatPrompt> process(ChatState state) {
        return Uni.createFrom().item(() -> prepare(state));
    }

    public ChatPrompt prepare(ChatState state) {
        var candidates = new ArrayList<ContextCandidate>();
        candidates.add(new ContextCandidate(state.scope(), state.messageId(), "user", state.message(),
            Optional.empty(), true));
        var evidence = state.evidence();
        for (int i = evidence.size() - 1; i >= 0; i--) {
            ChatEvidence entry = evidence.get(i);
            candidates.add(new ContextCandidate(state.scope(), entry.entryId(), entry.provenance(), entry.text(),
                entry.payloadReference().map(ignored -> entry.entryId()), false));
        }
        ChatWindow window = state.window();
        var selection = assembler.assemble(new ContextAssemblyRequest(
            state.scope(), candidates, window.contextLimitTokens(), window.instructionTokens(), 0,
            window.reservedOutputTokens(), window.maxEntryBytes()));
        if (!selection.fits()) {
            throw new IllegalArgumentException("chat model context cannot fit: "
                + selection.overflowReason().orElse("unknown"));
        }
        var order = new java.util.HashMap<String, Integer>();
        for (int i = 0; i < evidence.size(); i++) {
            order.put(evidence.get(i).entryId(), i);
        }
        List<ChatPromptEntry> context = selection.selected().stream()
            .filter(entry -> !entry.entryId().equals(state.messageId()))
            .sorted(Comparator.comparingInt(entry -> order.get(entry.entryId())))
            .map(entry -> new ChatPromptEntry(entry.provenance(), entry.text())).toList();
        return new ChatPrompt(state, context, state.message());
    }
}
