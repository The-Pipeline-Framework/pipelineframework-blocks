package org.pipelineframework.blocks.chat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.pipelineframework.connector.conversation.ConversationScope;

/** Explicit application-authorized working state. Client-supplied instances still require application validation. */
public record ChatState(
    ConversationScope scope, String messageId, String message, ChatWindow window,
    List<ChatEvidence> evidence, int omittedEntries
) {
    public static final int MAX_ENTRIES = 64;
    public static final int MAX_ENTRY_BYTES = 16_384;
    public static final int MAX_STATE_BYTES = 262_144;

    public ChatState {
        scope = Objects.requireNonNull(scope, "scope must not be null");
        messageId = ChatEvidence.required(messageId, "messageId");
        message = Objects.requireNonNull(message, "message must not be null");
        window = Objects.requireNonNull(window, "window must not be null");
        evidence = List.copyOf(Objects.requireNonNull(evidence, "evidence must not be null"));
        if (message.isBlank() || message.getBytes(StandardCharsets.UTF_8).length > MAX_ENTRY_BYTES) {
            throw new IllegalArgumentException("message must contain 1 to 16384 UTF-8 bytes");
        }
        if (omittedEntries < 0 || evidence.size() > MAX_ENTRIES) {
            throw new IllegalArgumentException("invalid carried evidence count");
        }
        var ids = new HashSet<String>();
        int bytes = fixedBytes(scope, messageId, message);
        for (ChatEvidence entry : evidence) {
            if (!ids.add(entry.entryId()) || entry.entryId().equals(messageId)) {
                throw new IllegalArgumentException("repeated chat evidence or message ID");
            }
            bytes += entryBytes(entry);
            if (bytes > MAX_STATE_BYTES) {
                throw new IllegalArgumentException("carried chat state exceeds 262144 UTF-8 bytes");
            }
        }
    }

    public static ChatState start(ConversationScope scope, String messageId, String message) {
        return new ChatState(scope, messageId, message, ChatWindow.DEFAULT, List.of(), 0);
    }

    /** The application calls this only after accepting a proposed reply and authorizing the next message. */
    public ChatState next(String nextMessageId, String nextMessage) {
        return new ChatState(scope, nextMessageId, nextMessage, window, evidence, omittedEntries);
    }

    /** Add one captured Query observation; evict oldest evidence when the carried-state bound is reached. */
    public ChatState appendEvidence(ChatEvidence entry) {
        var next = new ArrayList<>(evidence);
        next.add(Objects.requireNonNull(entry, "entry must not be null"));
        int omitted = omittedEntries;
        while (next.size() > MAX_ENTRIES || fixedBytes(scope, messageId, message) + evidenceBytes(next) > MAX_STATE_BYTES) {
            next.removeFirst();
            omitted++;
        }
        return new ChatState(scope, messageId, message, window, next, omitted);
    }

    ChatState proposedWithReply(String reply) {
        var next = new ArrayList<>(evidence);
        next.add(new ChatEvidence(messageId, "user", message, java.util.Optional.empty()));
        next.add(new ChatEvidence(replyId(messageId), "assistant", reply, java.util.Optional.empty()));
        int omitted = omittedEntries;
        String completedId = "completed:" + digest(messageId);
        while (next.size() > MAX_ENTRIES
                || fixedBytes(scope, completedId, reply) + evidenceBytes(next) > MAX_STATE_BYTES) {
            next.removeFirst();
            omitted++;
        }
        return new ChatState(scope, completedId, reply, window, next, omitted);
    }

    private static int fixedBytes(ConversationScope scope, String messageId, String message) {
        return utf8(scope.tenantId()) + utf8(scope.ownerId()) + utf8(scope.conversationId())
            + utf8(messageId) + utf8(message) + 32;
    }

    private static int evidenceBytes(List<ChatEvidence> entries) {
        return entries.stream().mapToInt(ChatState::entryBytes).sum();
    }

    private static int entryBytes(ChatEvidence entry) {
        return utf8(entry.entryId()) + utf8(entry.provenance()) + utf8(entry.text())
            + entry.payloadReference().map(ref -> utf8(ref.toString())).orElse(0) + 16;
    }

    private static int utf8(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }

    private static String replyId(String messageId) {
        return "reply:" + digest(messageId);
    }

    private static String digest(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
