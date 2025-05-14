package ChattingSystem;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {

    // A list to store all the DataOutputStream objects (used to send messages to clients)
    private static final List<DataOutputStream> clientOutputStreams = new ArrayList<>();

    // Method to start the server and accept client connections
    public void startServer() {
        try {
            // Create a ServerSocket that listens on port 6001
            ServerSocket serverSocket = new ServerSocket(6001);
            System.out.println("Chat Server started on port 6001");

            // Infinite loop to accept multiple clients
            while (true) {
                Socket socket = serverSocket.accept();  // Wait for client to connect
                System.out.println("New client connected: " + socket);

                // Create a new ClientHandler to manage the communication with the client
                ClientHandler clientHandler = new ClientHandler(socket);
                // Start a new thread to handle this particular client
                Thread thread = new Thread(clientHandler);
                thread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Inner class to handle each client connection
    static class ClientHandler implements Runnable {
        private final Socket socket;  // The socket for this particular client
        private DataInputStream din;  // Input stream for receiving messages from the client
        private DataOutputStream dout;  // Output stream for sending messages to the client

        // Constructor that accepts the socket for this client
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                // Initialize the input and output streams
                din = new DataInputStream(socket.getInputStream());
                dout = new DataOutputStream(socket.getOutputStream());

                // Add this client's output stream to the list of client output streams
                synchronized (clientOutputStreams) {
                    clientOutputStreams.add(dout);
                }

                // Infinite loop to keep receiving messages from this client
                while (true) {
                    String message = din.readUTF();  // Read a message from the client
                    System.out.println("Received: " + message);

                    // Broadcast the message to all clients
                    broadcastMessage(message);
                }

            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket);
            } finally {
                try {
                    // Remove the client's output stream from the list when they disconnect
                    if (dout != null) {
                        synchronized (clientOutputStreams) {
                            clientOutputStreams.remove(dout);
                        }
                    }
                    // Close the socket connection with the client
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Method to broadcast a message to all clients
        private void broadcastMessage(String message) throws IOException {
            synchronized (clientOutputStreams) {
                // Iterate through each output stream and send the message
                for (DataOutputStream out : clientOutputStreams) {
                    out.writeUTF(message);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Create an instance of ChatServer and start the server
        ChatServer server = new ChatServer();
        server.startServer();
    }
}