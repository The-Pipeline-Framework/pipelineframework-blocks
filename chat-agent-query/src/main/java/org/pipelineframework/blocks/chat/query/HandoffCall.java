package org.pipelineframework.blocks.chat.query;

/** Model-proposed Command arguments after connector canonicalisation and trusted-field injection. */
public record HandoffCall(String binding, String operation, String argumentsJson, String contextJson) { }
