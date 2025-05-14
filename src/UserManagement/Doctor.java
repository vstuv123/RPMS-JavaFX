package UserManagement;

import javafx.stage.Stage;

public class Doctor extends User {

    private String specialization;  // Doctor's field of expertise

    // Default constructor
    public Doctor() {
    }

    // Constructor initializing doctor details including specialization
    public Doctor(long id, String name, String email, String contactNumber,
                  String specialization, String dob, String role, String address) {
        super(id, name, email, contactNumber, dob, role, address);
        this.setSpecialization(specialization);
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // Launches the doctor-specific dashboard
    public void openDashboard() {
        new DoctorDashboard().start(new Stage());
    }
}
