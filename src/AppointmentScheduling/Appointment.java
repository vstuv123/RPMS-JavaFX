package AppointmentScheduling;

import java.time.LocalDate;
import java.time.LocalTime;

public class Appointment {

    // Fields to store details about the appointment
    private long appointmentId;  // Unique ID for the appointment
    private LocalDate date;  // The date of the appointment
    private LocalTime time;  // The time of the appointment
    private long doctorID;  // ID of the doctor assigned to the appointment
    private long patientID;  // ID of the patient for the appointment
    private String status;  // Status of the appointment (e.g., scheduled, completed, cancelled)

    // Constructor to initialize the Appointment object
    public Appointment(long appointmentId, LocalDate date, LocalTime time, long doctorID, long patientID, String status) {
        this.appointmentId = appointmentId;
        this.date = date;
        this.time = time;
        this.doctorID = doctorID;
        this.patientID = patientID;
        this.status = status;
    }

    // Getter method for the appointment ID
    public long getAppointmentId() {
        return appointmentId;
    }

    // Getter method for the time of the appointment
    public LocalTime getTime() {
        return time;
    }

    // Getter method for the date of the appointment
    public LocalDate getDate() {
        return date;
    }

    // Setter method for the date of the appointment (in case it needs to be updated)
    public void setDate(LocalDate date) {
        this.date = date;
    }

    // Getter method for the doctor ID
    public long getDoctorID() {
        return doctorID;
    }

    // Getter method for the patient ID
    public long getPatientID() {
        return patientID;
    }

    // Getter method for the status of the appointment
    public String getStatus() {
        return status;
    }

    // Override toString method to provide a string representation of the Appointment object
    @Override
    public String toString() {
        return "Appointment Date: " + getDate() + ", Doctor ID: " + getDoctorID() + ", " +
                "Patient ID: " + getPatientID() + ", Status: " + getStatus();
    }
}