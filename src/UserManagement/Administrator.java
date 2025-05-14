package UserManagement;

import javafx.stage.Stage;

public class Administrator extends User {

    // Default constructor
    public Administrator() {
    }

    // Constructor to initialize administrator with user details
    public Administrator(long id, String name, String email, String contactNumber,
                         String dob, String role, String address) {
        super(id, name, email, contactNumber, dob, role, address);
    }

    // Launches the admin dashboard for this administrator
    public void openDashboard() {
        new AdminDashboard().start(new Stage());
    }
}

