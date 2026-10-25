package org.pipelineframework.blocks.chat.query;

/** Inert, compiler-validated Query proposal from the LLM connector. */
public record AgentCall(String binding, String operation, String argumentsJson, String contextJson) { }
