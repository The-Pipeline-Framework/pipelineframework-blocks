package org.pipelineframework.blocks.chat;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import org.pipelineframework.repository.PayloadReference;

/** Application-accepted evidence carried with the computation. */
public record ChatEvidence(String entryId, String provenance, String text, Optional<PayloadReference> payloadReference) {
    public ChatEvidence {
        entryId = required(entryId, "entryId");
        provenance = required(provenance, "provenance");
        text = Objects.requireNonNull(text, "text must not be null");
        payloadReference = Objects.requireNonNull(payloadReference, "payloadReference must not be null");
        if (text.getBytes(StandardCharsets.UTF_8).length > ChatState.MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("evidence text exceeds the carried entry bound");
        }
        if (text.isBlank() && payloadReference.isEmpty()) {
            throw new IllegalArgumentException("evidence requires text or a payload reference");
        }
    }

    static String required(String value, String field) {
        String checked = Objects.requireNonNull(value, field + " must not be null");
        if (checked.isBlank() || checked.length() > 160 || !checked.equals(checked.strip())
                || checked.codePoints().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " must be a non-blank, unpadded identifier of at most 160 characters");
        }
        return checked;
    }
}
