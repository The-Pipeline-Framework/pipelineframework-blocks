package org.pipelineframework.blocks.chat.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.pipelineframework.blocks.chat.ChatEvidence;
import org.pipelineframework.blocks.chat.ChatState;
import org.pipelineframework.config.pipeline.PipelineJson;
import org.pipelineframework.service.ReactiveService;

/** A successful captured Query becomes bounded carried evidence for the second inference. */
@ApplicationScoped
public final class ReduceChatQueryObservationService
    implements ReactiveService<OperationObservation, ChatSynthesisPrompt> {
    private static final ObjectMapper JSON = PipelineJson.mapper();

    @Override
    public Uni<ChatSynthesisPrompt> process(OperationObservation observation) {
        return Uni.createFrom().item(() -> reduce(observation));
    }

    static ChatSynthesisPrompt reduce(OperationObservation observation) {
        Objects.requireNonNull(observation, "Query observation must not be null");
        String context;
        String text;
        String binding;
        String operation;
        if (observation instanceof OperationObservation.Result result) {
            OperationResultObservation value = result.value();
            requireQuery(value.kind());
            context = value.contextJson();
            text = value.resultJson();
            binding = value.binding();
            operation = value.operation();
        } else {
            OperationEmptyObservation value = ((OperationObservation.Empty) observation).value();
            requireQuery(value.kind());
            context = value.contextJson();
            text = "The Query returned no result (" + value.outcome() + ", " + value.code() + ").";
            binding = value.binding();
            operation = value.operation();
        }
        if (text == null || text.isBlank() || text.getBytes(StandardCharsets.UTF_8).length > ChatState.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException(
                "Query observation cannot be carried as bounded text; return a concise result or PayloadReference");
        }
        ChatState state;
        try {
            state = JSON.readValue(context, TrustedContext.class).state();
        } catch (Exception failure) {
            throw new IllegalArgumentException("captured Query observation has invalid carried chat state", failure);
        }
        String entryId = "query:" + java.util.UUID.nameUUIDFromBytes(
            (state.messageId() + "\u0000" + binding + "\u0000" + operation).getBytes(StandardCharsets.UTF_8));
        ChatState observed = state.appendEvidence(new ChatEvidence(
            entryId, "query:" + binding + "/" + operation, text, Optional.empty()));
        var prompt = new org.pipelineframework.blocks.chat.PrepareChatTurnService().prepare(observed);
        return new ChatSynthesisPrompt(prompt.state(), prompt.context(), prompt.message());
    }

    private static void requireQuery(String kind) {
        if (kind == null || !(kind.equalsIgnoreCase("QUERY") || kind.equalsIgnoreCase("tpf:query"))) {
            throw new IllegalArgumentException("ChatAgent tool path accepts Query observations only");
        }
    }

    private record TrustedContext(ChatState state) {
        private TrustedContext {
            state = Objects.requireNonNull(state, "carried chat state must not be null");
        }
    }
}
