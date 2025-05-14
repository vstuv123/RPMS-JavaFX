package ChattingSystem;

import Helper.Helper;
import UserManagement.Doctor;
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatDoctorClient extends Application {

    private long selectedPatient;  // The ID of the selected patient for chatting
    private Doctor doctor;  // The doctor object representing the logged-in doctor
    private ListView<ChatPreview> patientListView;  // List view to display patients
    private VBox messageBox;  // The VBox to contain the messages for the chat
    private TextField inputField;  // The input field for typing messages
    private Button sendButton;  // Button to send messages
    private DataInputStream din;  // Input stream for receiving data from the server
    private DataOutputStream dout;  // Output stream for sending data to the server
    private ScrollPane scrollPane;  // Scroll pane for the messages area

    @Override
    public void start(Stage stage) {
        doctor = (Doctor) SessionStorage.loggedInUser;  // Retrieve the logged-in doctor
        connectSocket();  // Establish connection to the server

        BorderPane root = new BorderPane();  // Create the root layout (BorderPane)

        // Left side: Patient list view
        patientListView = new ListView<>();
        patientListView.setPrefWidth(200);  // Set the preferred width of the patient list
        loadPatients();  // Load the list of patients

        // Add listener to update selected patient and load their chat messages
        patientListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedPatient = newVal.getPatientId();  // Set selected patient ID
                loadMessages();  // Load messages for the selected patient
            }
        });

        // Right side: Chat area
        messageBox = new VBox(6);  // VBox to hold the messages
        messageBox.setPadding(new Insets(10));  // Add padding to the message box
        scrollPane = new ScrollPane(messageBox);  // Add the message box inside a scroll pane
        scrollPane.setFitToWidth(true);  // Ensure the scroll pane fits the width

        // Input area for typing a message
        inputField = new TextField();
        inputField.setPromptText("Type message...");
        inputField.setMinWidth(500);  // Set minimum width of input field
        sendButton = new Button("Send");  // Create send button

        // Send button action handler
        sendButton.setOnAction(e -> sendMessage());
        sendButton.setDefaultButton(true);  // Set default button to send the message when 'Enter' is pressed

        HBox inputArea = new HBox(10, inputField, sendButton);  // Create input area
        inputArea.setPadding(new Insets(10));  // Add padding to input area

        VBox chatLayout = new VBox(scrollPane, inputArea);  // Combine message display and input area
        VBox.setVgrow(scrollPane, Priority.ALWAYS);  // Make the scroll pane grow to take available space

        root.setLeft(patientListView);  // Place the patient list on the left side
        root.setCenter(chatLayout);  // Place the chat layout in the center

        Scene scene = new Scene(root, 800, 500);  // Set the scene size and root layout
        stage.setScene(scene);
        stage.setTitle("Doctor Chat");
        stage.show();  // Display the stage

        listenForMessages();  // Start listening for incoming messages
    }

    // Method to connect to the server using a socket
    private void connectSocket() {
        new Thread(() -> {
            try {
                // Connect to the server at localhost:6001
                Socket socket = new Socket("127.0.0.1", 6001);
                din = new DataInputStream(socket.getInputStream());  // Initialize input stream
                dout = new DataOutputStream(socket.getOutputStream());  // Initialize output stream
            } catch (IOException e) {
                e.printStackTrace();  // Print the stack trace in case of an error
            }
        }).start();  // Start the connection in a new thread
    }

    // Method to load the list of patients assigned to the doctor
    private void loadPatients() {
        patientListView.getItems().clear();  // Clear existing items in the list
        HashMap<Long, String> chats = ChatController.getChatsWithLastMessage(doctor.getUnique_id());  // Get chats for the doctor

        // Loop through the chats and add each patient to the list view
        for (Map.Entry<Long, String> entry : chats.entrySet()) {
            long patientId = entry.getKey();
            String lastMessage = entry.getValue();
            Patient patient = Helper.fetchPatient(patientId);  // Fetch patient details

            if (patient != null) {
                String name = patient.getName();  // Get the patient's name
                patientListView.getItems().add(new ChatPreview(patientId, name, lastMessage));  // Add chat preview to list
            }
        }

        // Customize how the chat list cells appear
        patientListView.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ChatPreview item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox vbox = new VBox();  // Create a VBox to hold the patient info
                    vbox.setSpacing(3);
                    vbox.setPadding(new Insets(5, 10, 5, 10));

                    // Create a label for the patient's name
                    Label nameLabel = new Label(item.getPatientName());
                    nameLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #202124;");

                    // Create a label for the last message
                    Label messageLabel = new Label(item.getLastMessage());
                    messageLabel.setWrapText(true);  // Allow text to wrap
                    messageLabel.setStyle("-fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: normal; -fx-text-fill: #5f6368;");

                    vbox.getChildren().addAll(nameLabel, messageLabel);  // Add labels to VBox

                    // Highlight selected item with a background color
                    if (isSelected()) {
                        vbox.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 5;");
                    } else {
                        vbox.setStyle("-fx-background-color: transparent;");
                    }

                    setGraphic(vbox);  // Set the cell's graphic as the VBox
                }
            }
        });
    }

    // Method to load messages for the selected patient
    private void loadMessages() {
        Platform.runLater(() -> {
            messageBox.getChildren().clear();  // Clear existing messages
            int chatId = ChatController.getOrCreateChatId(doctor.getUnique_id(), selectedPatient);  // Get or create chat ID
            ArrayList<Message> messages = ChatController.getMessages(chatId);  // Fetch messages for the chat

            // Loop through and add each message to the message box
            for (Message msg : messages) {
                String sender = msg.getSenderRole();
                String content = msg.getContent();
                DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("dd MMMM yyyy hh:mm a");
                String formattedTime = msg.getTimestamp().format(displayFormat);
                addStyledMessage(sender, content, formattedTime);  // Add each message with style
            }

            // Auto-scroll to the latest message when a new message is added
            messageBox.heightProperty().addListener((obs, oldVal, newVal) -> {
                scrollPane.setVvalue(1.0);  // Scroll to the bottom
            });
        });
    }

    // Method to send a message to the selected patient
    private void sendMessage() {
        String msg = inputField.getText().trim();  // Get the text from the input field
        if (!msg.isEmpty()) {  // Only send if the message is not empty
            try {
                // Format the message to send to the server (doctorId:patientId:message)
                String formatted = doctor.getUnique_id() + ":" + selectedPatient + ":" + msg;
                dout.writeUTF(formatted);  // Send message to the server
                ChatController.saveMessage(doctor.getUnique_id(), selectedPatient, "DOCTOR", msg);  // Save message in DB

                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a"));
                addStyledMessage("DOCTOR", msg, timestamp);  // Display the message in the UI
                inputField.clear();  // Clear input field
                loadPatients();  // Reload the list of patients (to update last message)
            } catch (IOException ex) {
                ex.printStackTrace();  // Print error if sending fails
            }
        }
    }

    // Method to listen for incoming messages from the server
    private void listenForMessages() {
        new Thread(() -> {
            try {
                while (true) {
                    String received = din.readUTF();  // Read the incoming message
                    String[] parts = received.split(":", 3);
                    if (parts.length == 3) {
                        long fromId = Long.parseLong(parts[0]);
                        long toId = Long.parseLong(parts[1]);
                        String content = parts[2];

                        // If the message is for the current doctor and from the selected patient, display it
                        if (toId == doctor.getUnique_id() && fromId == selectedPatient) {
                            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"));
                            ChatController.saveMessage(fromId, toId, "PATIENT", content);  // Save patient message
                            addStyledMessage("PATIENT", content, timestamp);  // Display the message in the UI
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();  // Print the error if connection fails
                Platform.runLater(() -> addStyledMessage("SYSTEM", "❌ Disconnected from server", ""));  // Show error if disconnected
            }
        }).start();  // Start listening in a new thread
    }

    // Method to add a styled message to the message box
    private void addStyledMessage(String senderRole, String content, String timestamp) {
        VBox messageContainer = new VBox(5);  // Create a VBox to hold the message and timestamp
        messageContainer.setPadding(new Insets(5));

        Label messageLabel = new Label(content);
        messageLabel.setWrapText(true);  // Allow the message text to wrap
        messageLabel.setMaxWidth(300);  // Set maximum width for the message
        messageLabel.setStyle(
                senderRole.equals("DOCTOR") ?
                        "-fx-background-color: lightgreen; -fx-padding: 8; -fx-background-radius: 10;" :
                        "-fx-background-color: lightblue; -fx-padding: 8; -fx-background-radius: 10;"
        );

        Label timeLabel = new Label(timestamp);  // Create a label for the timestamp
        timeLabel.setStyle("-fx-font-size: 10; -fx-text-fill: gray;");  // Style the timestamp
        timeLabel.setWrapText(true);

        messageContainer.getChildren().addAll(messageLabel, timeLabel);  // Add the message and timestamp to the VBox

        HBox wrapper = new HBox(messageContainer);  // Create a wrapper HBox for alignment
        wrapper.setPadding(new Insets(5, 10, 5, 10));  // Add padding to the wrapper
        wrapper.setAlignment(
                senderRole.equals("DOCTOR") ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT  // Align message based on sender role
        );

        Platform.runLater(() -> messageBox.getChildren().add(wrapper));  // Add the message to the message box
    }
}