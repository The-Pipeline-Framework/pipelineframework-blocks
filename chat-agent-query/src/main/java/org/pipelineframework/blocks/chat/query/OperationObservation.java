package org.pipelineframework.blocks.chat.query;

public sealed interface OperationObservation permits OperationObservation.Result, OperationObservation.Empty {
    String discriminator();

    record Result(OperationResultObservation value) implements OperationObservation {
        @Override public String discriminator() { return "result"; }
    }

    record Empty(OperationEmptyObservation value) implements OperationObservation {
        @Override public String discriminator() { return "empty"; }
    }
}
