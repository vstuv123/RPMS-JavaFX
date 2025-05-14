package NotificationsAndReminders;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import resources.ConfigLoader;

public class SMSNotification implements Notifiable {

    // Twilio credentials and sender phone number are loaded from configuration
    public static final String ACCOUNT_SID = ConfigLoader.get("accountSid");
    public static final String AUTH_TOKEN = ConfigLoader.get("authToken");
    public static final String FROM_NUMBER = ConfigLoader.get("fromNumber");

    // Initialize Twilio once when the class is loaded
    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    // Sends a notification message via SMS to the recipient's phone number
    @Override
    public void sendNotification(ContactInfo contact, String subject, String message) {
        try {
            sendBulkSMS(contact.getPhoneNumber(), message);
        } catch (Exception e) {
            System.out.println("Failed to send SMS to " + contact.getPhoneNumber());
            e.printStackTrace(); // Print stack trace for debugging
        }
    }

    // Core method that sends the SMS using Twilio API
    public static void sendBulkSMS(String recipient, String messageBody) {
        Message.creator(
                new PhoneNumber(recipient),     // Recipient number
                new PhoneNumber(FROM_NUMBER),   // Sender number
                messageBody                     // SMS body content
        ).create();
    }
}