package DoctorPatientInteraction;

public class Prescription {
    // declaring attributes
    private long doctorID;
    private String timestamp;
    private String instructions;
    private String medicineName;
    private String notes;
    private String dosage;
    private String schedule;

    // defining parameterized constructor
    public Prescription(long doctorID, String instructions, String medicineName,
                        String dosage, String schedule, String notes, String timestamp) {
        this.doctorID = doctorID;
        this.instructions = instructions;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.schedule = schedule;
        this.notes = notes;
        this.timestamp = timestamp;
    }
    // getters
    public String getNotes() { return notes; }
    public String getTimestamp() { return timestamp; }
    public String getMedicineName() { return medicineName;}
    public String getDosage() {return dosage; }
    public String getSchedule() { return schedule;}
    public long getDoctorID() {return doctorID;}
    public String getInstructions() { return instructions;}

    // toString method
    @Override
    public String toString() {
        return "Doctor ID: " + doctorID + "\nInstructions: " + instructions +
                "\nMedications: " + medicineName + " (" + dosage + ", " + schedule + notes + ")";
    }
}
