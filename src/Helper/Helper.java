package Helper;

import Connection.Conn;
import NotificationsAndReminders.EmailNotification;
import NotificationsAndReminders.Notifiable;
import NotificationsAndReminders.ReminderService;
import NotificationsAndReminders.SMSNotification;
import UserManagement.Doctor;
import UserManagement.Patient;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class Helper {

    private ArrayList<Notifiable> notifier = new ArrayList<>();

    // Returns the current timestamp formatted for SQL datetime
    public static String currentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    // Formats LocalTime to a human-readable 12-hour format
    public static String formatTime(LocalTime time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("h:mm a");
        return time.format(formatter);
    }

    // Generates a pseudo-random 6-digit number (used for IDs or codes)
    public static long randomNumberGenerator() {
        Random random = new Random();
        return 100000 + random.nextLong(900000);
    }

    // Hashes a plain text password using BCrypt
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    // Checks a plain password against a hashed one
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    // Retrieves doctor ID based on email and table name
    public static long fetchDoctorID(String email, String tableName) {
        long id;
        String query = "select id from " + tableName + " where email = '" + email + "'";
        try (Conn conn = new Conn()) {
            ResultSet rs = conn.runQuery(query);
            if (rs.next()) {
                id = Long.parseLong(rs.getString("id"));
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    // Retrieves doctor details from the database using doctor ID
    public static Doctor fetchDoctor(long id) {
        String query = "select * from Doctor where id = '" + id + "'";
        Doctor d;
        try (Conn conn = new Conn()) {
            ResultSet rs = conn.runQuery(query);
            if (rs.next()) {
                d = new Doctor(Long.parseLong(rs.getString("id")), rs.getString("name"), rs.getString("email"),
                        rs.getString("contactNumber"), rs.getString("specialization"), rs.getString("dob"),
                        rs.getString("role"), rs.getString("address"));
                return d;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Retrieves patient details and links them to their assigned doctor
    public static Patient fetchPatient(long id) {
        String query = "select * from Patient where id = '" + id + "'";
        Patient p;
        try (Conn conn = new Conn()) {
            ResultSet rs = conn.runQuery(query);
            if (rs.next()) {
                long assignedTo = rs.getLong("assignedTo");
                String doctorEmail = Objects.requireNonNull(Helper.fetchDoctor(assignedTo)).getEmail();
                p = new Patient(Long.parseLong(rs.getString("id")), rs.getString("name"), rs.getString("email"),
                        rs.getString("contactNumber"), rs.getString("dob"), rs.getString("role"),
                        rs.getString("address"), rs.getString("emergencyEmail"),
                        rs.getLong("assignedTo"), assignedTo != 0 ? doctorEmail : null);
                return p;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Inserts an activity log for auditing user actions
    public static void insertLog(String userId, String actionType, String desc) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO logs (user_id, action_type, description, timestamp) VALUES (")
                .append(userId).append(", '")
                .append(actionType).append("', '")
                .append(desc).append("', '")
                .append(currentTimestamp()).append("')");

        try (Conn conn = new Conn()) {
            conn.runUpdate(query.toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Validates if a given patient ID exists in the database
    public static boolean isValidPatientId(long patientId) {
        boolean isValid = false;
        String query = "SELECT * FROM patient where id = " + patientId;

        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) {
            if (rs.next()) {
                isValid = true;
            }
        } catch (SQLException e) {
            isValid = false;
            e.getErrorCode(); // Swallowed silently – can be logged
        }
        return isValid;
    }

    // Builds and returns a list of notification mechanisms (Email & SMS)
    public ArrayList<Notifiable> reminderService() {
        notifier.add(new EmailNotification());
        notifier.add(new SMSNotification());
        return notifier;
    }

    // Generates a ReminderService configured with all notifiers
    public ReminderService generateReminderObject() {
        return new ReminderService(reminderService());
    }
}
