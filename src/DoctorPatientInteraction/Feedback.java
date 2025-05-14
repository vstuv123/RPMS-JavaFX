package DoctorPatientInteraction;

public class Feedback {
    // declaring attributes
    private String notes;
    private long doctorID;
    private String timestamp;

    // defining parameterized constructor
    public Feedback(long doctorId, String notes, String timestamp) {
        this.doctorID = doctorId;
        this.notes = notes;
        this.timestamp = timestamp;
    }
    // getters
    public String getTimestamp() {return timestamp;}
    public long getDoctorID() {
        return doctorID;
    }
    public String getNotes() { return notes; }
}