package ChattingSystem;

import Connection.Conn;
import Helper.Helper;
import UserManagement.Patient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class ChatController {

    // Method to get the list of patients assigned to a particular doctor
    public static ArrayList<Patient> getAssignedPatients(long doctorId) {
        ArrayList<Patient> patients = new ArrayList<>();

        // Query to fetch patients assigned to the given doctor
        String query = "SELECT * FROM Patient WHERE assignedTo=" + doctorId;
        try (Conn con = new Conn();  // Try-with-resources to ensure Conn is closed after use
             ResultSet rs = con.runQuery(query)) {

            // Loop through the result set and create Patient objects
            while (rs.next()) {
                long assignedTo = rs.getLong("assignedTo");

                // Fetch the email of the doctor assigned to the patient
                String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(assignedTo)).getEmail();

                // Add each patient to the list
                patients.add(new Patient(rs.getLong("id"), rs.getString("name"), rs.getString("email"),
                        rs.getString("contactNumber"), rs.getString("dob"), rs.getString("role"),
                        rs.getString("address"), rs.getString("emergencyEmail"), rs.getLong("assignedTo"),
                        assignedTo != 0 ? doctorEmail : null));
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }
        return patients;
    }

    // Method to get a map of patients and their last message with the doctor
    public static HashMap<Long, String> getChatsWithLastMessage(long doctorId) {
        HashMap<Long, String> patientToLastMessage = new HashMap<>();

        // Query to fetch the last message of each patient for the given doctor
        String query = "SELECT patient_id, last_message FROM chat WHERE doctor_id = " + doctorId;
        try (Conn con = new Conn();
             ResultSet rs = con.runQuery(query)) {

            // Loop through the result set and map patient IDs to the last message
            while (rs.next()) {
                long patientId = rs.getLong("patient_id");
                String lastMessage = rs.getString("last_message");
                patientToLastMessage.put(patientId, lastMessage);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }

        return patientToLastMessage;
    }

    // Method to get an existing chat ID or create a new one if it doesn't exist
    public static int getOrCreateChatId(long doctorId, long patientId) {
        int chatId = -1;

        // Query to check if a chat already exists between the doctor and the patient
        String selectQuery = "SELECT chat_id FROM chat WHERE doctor_id=" + doctorId + " AND patient_id=" + patientId;

        // If chat doesn't exist, create a new chat entry in the database
        String insertQuery = "INSERT INTO chat(doctor_id, patient_id, last_message) VALUES (" + doctorId + ", " + patientId + ", NULL)";
        try (Conn con = new Conn();
             ResultSet rs = con.runQuery(selectQuery)) {

            // If a chat exists, fetch its ID
            if (rs.next()) {
                chatId = rs.getInt("chat_id");
            } else {
                // If no chat exists, create a new one
                con.runUpdate(insertQuery);

                // Fetch the newly created chat ID
                ResultSet rs2 = con.runQuery(selectQuery);
                if (rs2.next()) {
                    chatId = rs2.getInt("chat_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }
        return chatId;
    }

    // Method to fetch all messages for a given chat ID
    public static ArrayList<Message> getMessages(int chatId) {
        ArrayList<Message> list = new ArrayList<>();

        // Query to fetch all messages in a particular chat, ordered by timestamp
        try (Conn con = new Conn();
             ResultSet rs = con.runQuery("SELECT * FROM message WHERE chat_id=" + chatId + " ORDER BY timestamp")) {

            // Loop through the result set and create Message objects
            while (rs.next()) {
                list.add(new Message(rs.getInt("message_id"), rs.getInt("chat_id"),
                        rs.getString("sender_role"), rs.getString("content"),
                        rs.getTimestamp("timestamp").toLocalDateTime(), rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }
        return list;
    }

    // Method to get the sender's ID based on the role in the chat
    public static long getSenderId(int chatId, String senderRole) {
        String query;
        // Depending on whether the sender is a doctor or a patient, the query differs
        if (senderRole.equals("DOCTOR")) {
            query = "SELECT doctor_id FROM chat WHERE chat_id=" + chatId;
        } else {
            query = "SELECT patient_id FROM chat WHERE chat_id=" + chatId;
        }

        try (Conn con = new Conn();
             ResultSet rs = con.runQuery(query)) {
            if (rs.next()) {
                if (senderRole.equals("DOCTOR")) {
                    return rs.getLong("doctor_id");
                } else {
                    return rs.getLong("patient_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }
        return -1;  // Return -1 if no sender is found
    }

    // Method to save a new message and update the last message for the chat
    public static void saveMessage(long doctorId, long patientId, String senderRole, String content) {
        int chatId = getOrCreateChatId(doctorId, patientId);  // Get or create the chat ID

        try (Conn con = new Conn()) {
            // Insert the new message into the message table
            String insertMessage = "INSERT INTO message(chat_id, sender_role, content, status) " +
                    "VALUES (" + chatId + ", '" + senderRole + "', '" + content + "', 'SENT')";
            con.runUpdate(insertMessage);

            // Update the last message in the chat table
            String updateLastMessage = "UPDATE chat SET last_message = '" + content + "' WHERE chat_id = " + chatId;
            con.runUpdate(updateLastMessage);

        } catch (SQLException e) {
            e.printStackTrace();  // Handle any SQL exceptions
        }
    }
}