package NotificationsAndReminders;

import java.util.ArrayList;

public class ReminderService {
    // List of channels (e.g., email, SMS) to send notifications through
    private final ArrayList<Notifiable> notifiers;

    // Constructor to initialize the service with the given notifiers
    public ReminderService(ArrayList<Notifiable> notifiers) {
        this.notifiers = notifiers;
    }

    // Sends a reminder to the specified contact via all available notifiers
    public void sendReminder(ContactInfo contact, String subject, String message) {
        for (Notifiable notifier : notifiers) {
            notifier.sendNotification(contact, subject, message);
        }
    }
}

