package ChattingSystem;

import Helper.Helper;
import UserManagement.Patient;
import UserManagement.SessionStorage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;

public class ChatPatientClient extends Application {

    private DataOutputStream dout;  // Data output stream for sending messages to the server
    private DataInputStream din;    // Data input stream for receiving messages from the server
    private VBox messageBox;  // VBox to hold all the messages in the chat
    private TextField inputField;  // Input field where the patient types their message
    private Patient patient;  // The logged-in patient
    private long assignedDoctorId;  // The doctor assigned to this patient
    private ScrollPane scrollPane;  // Scrollable area for displaying messages

    @Override
    public void start(Stage stage) {
        patient = (Patient) SessionStorage.loggedInUser;  // Retrieve the logged-in patient
        assignedDoctorId = Objects.requireNonNull(Helper.fetchDoctor(patient.getAssignedTo())).getUnique_id();  // Fetch the assigned doctor's ID

        BorderPane root = new BorderPane();  // Root layout for the UI

        // Create the message area (VBox for holding messages)
        messageBox = new VBox(6);
        messageBox.setPadding(new Insets(10));
        scrollPane = new ScrollPane(messageBox);  // Wrap messageBox in a scroll pane
        scrollPane.setFitToWidth(true);  // Make scroll pane fit the width of the screen

        // Input field where patient can type a message
        inputField = new TextField();
        inputField.setPromptText("Type your message...");  // Placeholder text in the input field
        inputField.setMinWidth(700);  // Set minimum width of the input field

        // Button to send the message
        Button sendButton = new Button("Send");
        sendButton.setOnAction(e -> sendMessage());  // Send message when clicked
        sendButton.setDefaultButton(true);  // Allow sending message on pressing 'Enter' key

        // Create the input area with the input field and the send button
        HBox inputArea = new HBox(10, inputField, sendButton);
        inputArea.setPadding(new Insets(10));

        // Combine message display and input area into the layout
        VBox chatLayout = new VBox(scrollPane, inputArea);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);  // Make the scroll pane grow to take available space

        root.setCenter(chatLayout);  // Set the center of the root layout to the chat layout

        Scene scene = new Scene(root, 800, 500);  // Create the scene
        stage.setScene(scene);
        stage.setTitle("Patient Chat");  // Set the window title
        stage.show();  // Show the window

        new Thread(this::connectToServer).start();  // Start a new thread to connect to the server
        loadMessages();  // Load any previous messages when the chat window opens
    }

    // Method to connect to the server
    private void connectToServer() {
        try {
            Socket socket = new Socket("127.0.0.1", 6001);  // Connect to the server at localhost:6001
            din = new DataInputStream(socket.getInputStream());  // Initialize input stream
            dout = new DataOutputStream(socket.getOutputStream());  // Initialize output stream

            // Continuously listen for incoming messages
            while (true) {
                String msg = din.readUTF();  // Read message from server
                String[] parts = msg.split(":", 3);  // Split message by ":" into sender, receiver, and content

                if (parts.length == 3) {
                    long fromId = Long.parseLong(parts[0]);
                    long toId = Long.parseLong(parts[1]);
                    String content = parts[2];

                    // If the message is for this patient and from the assigned doctor, display it
                    if (toId == patient.getUnique_id() && fromId == assignedDoctorId) {
                        ChatController.saveMessage(fromId, toId, "DOCTOR", content);  // Save message in the database
                        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"));
                        addStyledMessage("DOCTOR", content, timestamp);  // Display the received message in the UI
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            Platform.runLater(() -> addStyledMessage("SYSTEM", "❌ Disconnected from server", ""));  // Show error if disconnected
        }
    }

    // Method to send a message to the assigned doctor
    private void sendMessage() {
        String msg = inputField.getText().trim();  // Get the text entered by the patient
        if (!msg.isEmpty()) {  // Only send if the message is not empty
            try {
                // Format message in the form: patientId:doctorId:message
                String formatted = patient.getUnique_id() + ":" + assignedDoctorId + ":" + msg;
                dout.writeUTF(formatted);  // Send the message to the server
                ChatController.saveMessage(assignedDoctorId, patient.getUnique_id(), "PATIENT", msg);  // Save message in the database

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"));
                addStyledMessage("PATIENT", msg, timestamp);  // Display the sent message in the UI
                inputField.clear();  // Clear the input field after sending
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Method to load messages from the database for the chat between this patient and the doctor
    private void loadMessages() {
        Platform.runLater(() -> {
            messageBox.getChildren().clear();  // Clear existing messages
            int chatId = ChatController.getOrCreateChatId(assignedDoctorId, patient.getUnique_id());  // Get or create a chat ID
            ArrayList<Message> messages = ChatController.getMessages(chatId);  // Fetch messages from the database

            // Loop through each message and display it
            for (Message msg : messages) {
                String sender = msg.getSenderRole();
                String content = msg.getContent();
                DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm a");
                String formattedTime = msg.getTimestamp().format(displayFormat);
                addStyledMessage(sender, content, formattedTime);  // Add each message to the chat
            }

            // Auto-scroll to the latest message when new messages are added
            messageBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                scrollPane.setVvalue(1.0);  // Scroll to the bottom
            });
        });
    }

    // Method to add a styled message to the message box
    private void addStyledMessage(String senderRole, String content, String timestamp) {
        VBox messageContainer = new VBox(5);  // Create a VBox to hold the message and timestamp
        messageContainer.setPadding(new Insets(5));

        // Create a label for the message content
        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);  // Allow the message text to wrap
        messageLabel.setMaxWidth(300);  // Set a maximum width for the message
        messageLabel.setStyle(
                senderRole.equals("PATIENT") ?
                        "-fx-background-color: lightgreen; -fx-padding: 8; -fx-background-radius: 10;" :
                        "-fx-background-color: lightblue; -fx-padding: 8; -fx-background-radius: 10;"
        );

        // Create a label for the timestamp of the message
        Label timeLabel = new Label(timestamp);
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");  // Style the timestamp
        timeLabel.setWrapText(true);

        messageContainer.getChildren().addAll(messageLabel, timeLabel);  // Add message and timestamp to the VBox

        HBox wrapper = new HBox(messageContainer);  // Create a wrapper HBox for alignment
        wrapper.setPadding(new Insets(5, 10, 5, 10));  // Add padding to the wrapper
        wrapper.setAlignment(
                senderRole.equals("PATIENT") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT  // Align message based on sender role
        );

        Platform.runLater(() -> messageBox.getChildren().add(wrapper));  // Add the message to the message box
    }
}