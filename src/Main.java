
import ChattingSystem.ChatServer;
import UserManagement.Splash;

public class Main {

    public static void main(String[] args) {
        // Start the chat server in a separate thread
        new Thread(() -> {
            ChatServer server = new ChatServer();
            server.startServer();  // Begin accepting client connections
        }).start();

        // Launch the application UI
        Splash.launch(Splash.class, args);
    }
}
