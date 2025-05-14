package VideoConsultations;

import java.time.LocalDateTime;

public class VideoCall {
    // defining all attributes
    private long id;
    private long doctorId;
    private long patientId;
    private String meetingLink;
    private LocalDateTime scheduledDateTime;
    private String note;
    private String status;

    // defining constructor
    public VideoCall(long id, long doctorId, long patientId, String meetingLink, LocalDateTime scheduledDateTime, String note, String status) {
        this.id = id;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.meetingLink = meetingLink;
        this.scheduledDateTime = scheduledDateTime;
        this.note = note;
        this.status = status;
    }

    // getters
    public long getId() {return id;}
    public long getDoctorId() { return doctorId; }
    public long getPatientId() { return patientId; }
    public String getMeetingLink() { return meetingLink; }
    public LocalDateTime getScheduledDateTime() { return scheduledDateTime; }
    public String getNote() { return note; }
    public String getStatus() { return status; }
}

