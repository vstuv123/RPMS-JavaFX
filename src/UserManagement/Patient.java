package UserManagement;

import AppointmentScheduling.Appointment;
import HealthDataHandling.VitalSign;
import javafx.stage.Stage;
import java.util.ArrayList;

public class Patient extends User {

    // Fields specific to the Patient class
    private long assignedTo; // Doctor assigned to the patient
    private String assignedToEmail; // Email of the assigned doctor
    private String emergencyEmail; // Emergency contact email for the patient

    // Lists to store vital signs and appointments of the patient
    private ArrayList<VitalSign> vitals;
    private ArrayList<Appointment> patientAppointments;

    // Default constructor
    public Patient() {
    }

    // Constructor with basic patient information
    public Patient(long id, String name, String email, String contactNumber,
                   String dob, String role, String address) {
        super(id, name, email, contactNumber, dob, role, address);
    }

    // Constructor that includes emergency email, assigned doctor, and assigned doctor's email
    public Patient(long id, String name, String email, String contactNumber,
                   String dob, String role, String address, String emergencyEmail, long assignedTo, String assignedToEmail) {
        this(id, name, email, contactNumber, dob, role, address);
        this.emergencyEmail = emergencyEmail;
        this.assignedTo = assignedTo;
        this.setAssignedToEmail(assignedToEmail); // Set the doctor's email
    }

    // Getter method for emergency email
    public String getEmergencyEmail() {
        return emergencyEmail;
    }

    // Getter method for the assigned doctor's ID
    public long getAssignedTo() {
        return assignedTo;
    }

    // Getter method for the assigned doctor's email
    public String getAssignedToEmail() {
        return assignedToEmail;
    }

    // Setter method for the assigned doctor's email
    public void setAssignedToEmail(String assignedToEmail) {
        this.assignedToEmail = assignedToEmail;
    }

    // Adds a vital sign to the patient's list of vitals
    public void addVitalSign(VitalSign vital) {
        vitals = new ArrayList<>();
        vitals.add(vital); // Adds the new vital sign to the list
    }

    // Adds an appointment to the patient's list of appointments
    public void addPatientAppointments(Appointment ap) {
        patientAppointments = new ArrayList<>();
        patientAppointments.add(ap); // Adds the new appointment to the list
    }

    // Opens the patient's dashboard in a new window
    public void openDashboard() {
        new PatientDashboard().start(new Stage()); // Launches the PatientDashboard GUI
    }
}
