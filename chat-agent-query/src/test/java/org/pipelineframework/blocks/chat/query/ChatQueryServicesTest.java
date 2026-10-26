package org.pipelineframework.blocks.chat.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.pipelineframework.blocks.chat.ChatState;
import org.pipelineframework.blocks.chat.ChatResult;
import org.pipelineframework.config.pipeline.PipelineJson;
import org.pipelineframework.connector.conversation.ConversationScope;

class ChatQueryServicesTest {
    private static final ConversationScope SCOPE = new ConversationScope("tenant", "owner", "conversation");

    @Test
    void oneSuccessfulQueryCarriesEvidenceIntoSynthesisContext() throws Exception {
        ChatState state = ChatState.start(SCOPE, "message-1", "What is the court address?");
        String context = PipelineJson.mapper().writeValueAsString(Map.of("state", state));
        var observation = new OperationObservation.Result(new OperationResultObservation(
            "venue", "address.lookup", "tpf:query", 1, "found", "found", "{}", context,
            "VenueAddress", "{\"street\":\"Main Street 3\"}"));

        ChatSynthesisPrompt prompt = ReduceChatQueryObservationService.reduce(observation);

        assertEquals(state.scope(), prompt.state().scope());
        assertEquals(1, prompt.state().evidence().size());
        assertTrue(prompt.context().getFirst().text().contains("Main Street 3"));
        assertEquals("What is the court address?", prompt.message());
    }

    @Test
    void rejectsCommandsAndOversizedObservationsAsFailures() throws Exception {
        ChatState state = ChatState.start(SCOPE, "message-1", "Find venue");
        String context = PipelineJson.mapper().writeValueAsString(Map.of("state", state));
        assertThrows(IllegalArgumentException.class, () -> ReduceChatQueryObservationService.reduce(
            new OperationObservation.Result(new OperationResultObservation(
                "venue", "address.lookup", "tpf:command", 1, "found", "found", "{}", context,
                "VenueAddress", "{}"))));
        assertThrows(IllegalArgumentException.class, () -> ReduceChatQueryObservationService.reduce(
            new OperationObservation.Result(new OperationResultObservation(
                "venue", "address.lookup", "tpf:query", 1, "found", "found", "{}", context,
                "VenueAddress", "x".repeat(ChatState.MAX_ENTRY_BYTES + 1)))));
    }

    @Test
    void reportsObservationProvenanceErrorsWithoutBlamingCarriedState() throws Exception {
        ChatState state = ChatState.start(SCOPE, "message-1", "Find venue");
        String context = PipelineJson.mapper().writeValueAsString(Map.of("state", state));
        var observation = new OperationObservation.Result(new OperationResultObservation(
            "x".repeat(160), "address.lookup", "tpf:query", 1, "found", "found", "{}", context,
            "VenueAddress", "{\"street\":\"Main Street 3\"}"));

        var failure = assertThrows(IllegalArgumentException.class,
            () -> ReduceChatQueryObservationService.reduce(observation));
        assertTrue(failure.getMessage().contains("provenance"), failure.getMessage());
    }

    @Test
    void commandHandoffCarriesStateAndProposalWithoutDispatch() throws Exception {
        ChatState state = ChatState.start(SCOPE, "message-2", "Open a match");
        String context = PipelineJson.mapper().writeValueAsString(Map.of("state", state));
        HandoffCall call = new HandoffCall("application", "match.open", "{\"hour\":19}", context);

        ChatResult result = ProposeChatHandoffService.propose(call);

        assertTrue(result.reply().isEmpty());
        assertEquals(state, result.handoff().orElseThrow().state());
        assertEquals("COMMAND", result.handoff().orElseThrow().proposal().kind());
        assertEquals("application", result.handoff().orElseThrow().proposal().binding());
        assertEquals("match.open", result.handoff().orElseThrow().proposal().operation());
        assertEquals("{\"hour\":19}", result.handoff().orElseThrow().proposal().argumentsJson());
        assertThrows(IllegalArgumentException.class, () -> ProposeChatHandoffService.propose(
            new HandoffCall("application", "match.open", "{}", "{}")));
    }
}
