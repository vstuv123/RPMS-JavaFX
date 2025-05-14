package UserManagement;

public abstract class User {

    // Declaring user attributes (fields)
    private long unique_id;  // Unique identifier for the user
    private String name;     // User's name
    private String email;    // User's email address
    private String contactNumber; // User's contact number
    private String dob;      // User's date of birth
    private String role;     // User's role (e.g., Doctor, Patient, etc.)
    private String address;  // User's address

    // Default constructor
    public User() {
    }

    // Parameterized constructor to initialize the fields of the user
    public User(long id, String name, String email, String contactNumber,
                String dob, String role, String address) {
        this.unique_id = id;
        this.name = name;
        this.email = email;
        this.contactNumber = contactNumber;
        this.dob = dob;
        this.role = role;
        this.address = address;
    }

    // Getter method for unique_id
    public long getUnique_id() {
        return unique_id;
    }

    // Getter method for name
    public String getName() {
        return name;
    }

    // Getter method for email
    public String getEmail() {
        return email;
    }

    // Getter method for contact number
    public String getContactNumber() {
        return contactNumber;
    }

    // Getter method for date of birth
    public String getDob() {
        return dob;
    }

    // Getter method for address
    public String getAddress() {
        return address;
    }

    // Getter method for role
    public String getRole() {
        return role;
    }

    // Abstract method for opening the dashboard,
    // to be implemented by subclasses
    public abstract void openDashboard();
}
