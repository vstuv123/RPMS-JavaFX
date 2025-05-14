package EmergencyAlertSystem;

import resources.ConfigLoader;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationService {

    private static final Logger LOGGER = Logger.getLogger(NotificationService.class.getName());

    /**
     * Sends a plain text email to the specified recipient.
     *
     * @param to      Recipient's email address.
     * @param subject Subject of the email.
     * @param content Body of the email (plain text).
     */
    public void sendEmail(String to, String subject, String content) {
        String senderEmail = ConfigLoader.get("senderEmail");
        String senderPassword = ConfigLoader.get("senderPassword");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ConfigLoader.get("smtpServer"));
        props.put("mail.smtp.port", ConfigLoader.get("smtpPort"));

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);

            // Use plain text
            message.setText(content);

            Transport.send(message);
            LOGGER.info("Email sent successfully to " + to);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send email to " + to + ": " + e.getMessage(), e);
        }
    }

    /**
     * Sends an HTML-formatted email.
     *
     * @param to      Recipient's email address.
     * @param subject Subject of the email.
     * @param html    HTML content of the email.
     */
    public void sendHtmlEmail(String to, String subject, String html) {
        String senderEmail = ConfigLoader.get("senderEmail");
        String senderPassword = ConfigLoader.get("senderPassword");

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", ConfigLoader.get("smtpServer"));
        props.put("mail.smtp.port", ConfigLoader.get("smtpPort"));

        Session session = Session.getInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(senderEmail, senderPassword);
                    }
                });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(senderEmail));
            message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );
            message.setSubject(subject);

            // Set HTML content
            message.setContent(html, "text/html");

            Transport.send(message);
            LOGGER.info("HTML email sent successfully to " + to);
        } catch (MessagingException e) {
            LOGGER.log(Level.SEVERE, "Failed to send HTML email to " + to + ": " + e.getMessage(), e);
        }
    }
}
