package NotificationsAndReminders;

import EmergencyAlertSystem.NotificationService;

public class EmailNotification implements Notifiable {
    // Instance of NotificationService to send email notifications
    NotificationService ns = new NotificationService();

    // Override of the sendNotification method from Notifiable interface
    @Override
    public void sendNotification(ContactInfo contact, String subject, String htmlMessageContent) {
        // Sends an email using the NotificationService with the provided contact's email, subject, and message content
        ns.sendEmail(contact.getEmail(), subject, htmlMessageContent);
    }
}
