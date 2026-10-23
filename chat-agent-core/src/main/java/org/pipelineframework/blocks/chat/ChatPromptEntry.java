package org.pipelineframework.blocks.chat;

/** A selected, model-visible excerpt; references and authority remain in ChatState. */
public record ChatPromptEntry(String provenance, String text) {}
