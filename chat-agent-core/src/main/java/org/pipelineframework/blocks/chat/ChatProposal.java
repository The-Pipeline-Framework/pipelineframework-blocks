package org.pipelineframework.blocks.chat;

/** An inert proposal. A producing Block must validate operation arguments against its pinned contract. */
public record ChatProposal(String binding, String operation, String argumentsJson, String kind) {
    public ChatProposal {
        binding = ChatEvidence.required(binding, "binding");
        operation = ChatEvidence.required(operation, "operation");
        argumentsJson = java.util.Objects.requireNonNull(argumentsJson, "argumentsJson must not be null");
        kind = ChatEvidence.required(kind, "kind");
        if (!kind.equals("QUERY") && !kind.equals("COMMAND")) {
            throw new IllegalArgumentException("proposal kind must be QUERY or COMMAND");
        }
    }
}
