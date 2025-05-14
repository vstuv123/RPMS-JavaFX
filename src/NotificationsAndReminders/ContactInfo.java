package NotificationsAndReminders;

public class ContactInfo {
    private String email;  // defining attributes
    private String phoneNumber;

    // defining constructor
    public ContactInfo(String email, String phoneNumber) {
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    // getters
    public String getEmail() {
        return email;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
}

