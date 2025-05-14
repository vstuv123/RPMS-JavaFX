package ChattingSystem;

public class ChatPreview {
    private final String patientName;  // The name of the patient in the chat
    private final String lastMessage;  // The last message sent in the chat
    private final long patientId;  // The unique ID of the patient

    // Constructor to initialize the ChatPreview object with patientId, patientName, and lastMessage
    public ChatPreview(long patientId, String patientName, String lastMessage) {
        this.patientId = patientId;
        this.patientName = patientName;
        this.lastMessage = lastMessage;
    }

    // Getter method for patientId
    public long getPatientId() {
        return patientId;
    }

    // Getter method for patientName
    public String getPatientName() {
        return patientName;
    }

    // Getter method for the last message in the chat
    public String getLastMessage() {
        return lastMessage;
    }

    // Optional override for toString() to return patientName for debugging
    @Override
    public String toString() {
        return patientName;  // Just returns the patient's name as a string
    }
}