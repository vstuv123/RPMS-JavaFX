package NotificationsAndReminders;

import Connection.Conn;
import Helper.Helper;
import UserManagement.Patient;
import resources.ConfigLoader;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class SendRemindersPatient {

    Helper helper = new Helper();
    ReminderService remainder = helper.generateReminderObject();

    // Entry point to send medication reminders to patients based on prescription schedules
    public void sendDailyDoseReminders() {
        String query = "select * from prescription";
        try (Conn conn = new Conn();
             ResultSet rs = conn.runQuery(query)) {

            while (rs.next()) {
                long patientId = rs.getLong("patient_id");
                String medicineName = rs.getString("medicine_name");
                String dosage = rs.getString("dosage");
                String schedule = rs.getString("schedule");

                // Check if it's the correct time to send a reminder for this schedule
                if (shouldSendReminderNow(schedule)) {
                    String message = "Reminder: Take " + dosage + " of " + medicineName +
                            " as per your " + schedule.toLowerCase() + " schedule.";

                    // Fetch the patient information and send the reminder
                    Patient p = Helper.fetchPatient(patientId);
                    if (p != null) {
                        ContactInfo c1 = new ContactInfo(p.getEmail(), ConfigLoader.get("contactNumber"));
                        remainder.sendReminder(c1, "Medication Reminder: It's time to take your medicine", message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Log error for debugging if query fails
        }
    }

    // Determines whether the current time falls within the appropriate window for the given schedule
    private boolean shouldSendReminderNow(String schedule) {
        LocalTime now = LocalTime.now();

        switch (schedule.toLowerCase()) {
            case "morning":
                return now.isAfter(LocalTime.of(7, 0)) && now.isBefore(LocalTime.of(10, 0));
            case "afternoon":
                return now.isAfter(LocalTime.of(12, 0)) && now.isBefore(LocalTime.of(15, 0));
            case "evening":
                return now.isAfter(LocalTime.of(18, 0)) && now.isBefore(LocalTime.of(21, 0));
            default:
                return false; // Unsupported schedule
        }
    }
}
