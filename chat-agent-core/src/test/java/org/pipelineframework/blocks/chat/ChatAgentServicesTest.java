package org.pipelineframework.blocks.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.pipelineframework.connector.conversation.ConversationScope;
import org.pipelineframework.repository.PayloadReference;

class ChatAgentServicesTest {
    private static final ConversationScope SCOPE = new ConversationScope("tenant", "owner", "conversation");

    @Test
    void oneInferencePreparationCarriesStateWithoutHistoryAccess() {
        ChatState state = ChatState.start(SCOPE, "m1", "What did I say?");
        ChatPrompt prompt = new PrepareChatTurnService().prepare(state);
        assertEquals(state, prompt.state());
        assertEquals("What did I say?", prompt.message());
        assertTrue(prompt.context().isEmpty());

        ChatReply reply = new CompleteChatTurnService()
            .process(new ChatCompletion(state, new ChatModelReply("You asked me a question.")))
            .await().indefinitely().reply().orElseThrow();
        assertEquals("You asked me a question.", reply.text());
        assertEquals(2, reply.proposedNextState().evidence().size());
        ChatState continued = reply.proposedNextState().next("m2", "What question?");
        ChatPrompt second = new PrepareChatTurnService().prepare(continued);
        assertEquals(List.of("user", "assistant"), second.context().stream()
            .map(ChatPromptEntry::provenance).toList());
        assertEquals("What question?", second.message());
    }

    @Test
    void largeImmutableContentStaysReferencedWhileOnlyExcerptIsVisible() {
        PayloadReference ref = new PayloadReference("repo", "bucket", "object", "text/plain", "raw",
            "sha256", 100_000, "v1", Map.of(), Optional.empty());
        ChatEvidence evidence = new ChatEvidence("tool-1", "tool", "x".repeat(6_000), Optional.of(ref));
        ChatState state = new ChatState(SCOPE, "m2", "Summarize it", ChatWindow.DEFAULT,
            List.of(evidence), 0);
        ChatPrompt prompt = new PrepareChatTurnService().prepare(state);
        assertEquals(ref, prompt.state().evidence().getFirst().payloadReference().orElseThrow());
        assertFalse(prompt.context().isEmpty());
        assertTrue(prompt.context().getFirst().text().length() <= ChatWindow.DEFAULT.maxEntryBytes());
    }

    @Test
    void carriedStateHasItsOwnDeterministicBound() {
        var entries = new ArrayList<ChatEvidence>();
        for (int i = 0; i < ChatState.MAX_ENTRIES; i++) {
            entries.add(new ChatEvidence("e" + i, "user", "entry " + i, Optional.empty()));
        }
        ChatState state = new ChatState(SCOPE, "m1", "new message", ChatWindow.DEFAULT, entries, 0);
        ChatState proposed = state.proposedWithReply("answer");
        assertEquals(ChatState.MAX_ENTRIES, proposed.evidence().size());
        assertEquals(2, proposed.omittedEntries());
        assertEquals("e2", proposed.evidence().getFirst().entryId());
        assertThrows(IllegalArgumentException.class, () -> new ChatState(SCOPE, "m1", "message",
            ChatWindow.DEFAULT, entries.subList(0, 2).stream().map(e ->
                new ChatEvidence("same", "user", e.text(), Optional.empty())).toList(), 0));
    }

    @Test
    void requiredMessageThatCannotFitFailsBeforeInference() {
        ChatState state = new ChatState(SCOPE, "m1", "x".repeat(1_000),
            new ChatWindow(1_000, 200, 300, 100), List.of(), 0);
        assertThrows(IllegalArgumentException.class, () -> new PrepareChatTurnService().prepare(state));
    }
}
