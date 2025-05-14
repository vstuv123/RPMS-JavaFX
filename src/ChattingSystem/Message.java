package ChattingSystem;

import java.time.LocalDateTime;

public class Message {
    private int messageId;
    private int chatId;
    private String content;
    private String senderRole;
    private LocalDateTime timestamp;
    private String status;

    // ✅ Constructor
    public Message(int messageId, int chatId, String senderRole, String content, LocalDateTime timestamp, String status) {
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderRole = senderRole;
        this.content = content;
        this.timestamp = timestamp;
        this.status = status;
    }

    // ✅ Getters
    public int getMessageId() {
        return messageId;
    }

    public int getChatId() {
        return chatId;
    }

    public String getContent() {
        return content;
    }

    public String getSenderRole() {
        return senderRole;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    // toString method
    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", chatId=" + chatId +
                ", content='" + content + '\'' +
                ", senderRole='" + senderRole + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}

