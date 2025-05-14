package NotificationsAndReminders;

// interface
public interface Notifiable {
    // defining method in interface which has to be implemented b class that implements interface
    void sendNotification(ContactInfo contact, String subject, String htmlMessageContent);
}
