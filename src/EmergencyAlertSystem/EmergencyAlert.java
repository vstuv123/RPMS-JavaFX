package EmergencyAlertSystem;

import HealthDataHandling.VitalSign;
import Helper.Helper;
import UserManagement.Doctor;
import UserManagement.Patient;

import java.util.Objects;

public class EmergencyAlert {

    NotificationService ns = new NotificationService();

    /**
     * Evaluates the vital signs of a patient to determine if they are in a critical condition.
     * If any parameter crosses safe thresholds, an alert is triggered to the assigned doctor.
     *
     * @param vitals The vital signs to evaluate.
     * @return true if any value is in the critical range, false otherwise.
     */
    public boolean checkVitals(VitalSign vitals) {
        boolean isCritical = false;
        String subject = "🚑 Emergency Alert: Patient in Critical Condition!";

        // Check heart rate thresholds
        if (vitals.getHeartRate() > VitalsThreshold.HEART_RATE_MAX ||
                vitals.getHeartRate() < VitalsThreshold.HEART_RATE_MIN) {
            triggerAlert(vitals.getPatientID(),
                    "Critical Alert: Abnormal Heart Rate Detected",
                    "The patient's heart rate is critical: " + vitals.getHeartRate() + " bpm. Immediate attention required.");
            isCritical = true;
        }

        // Check temperature thresholds
        if (vitals.getTemperature() > VitalsThreshold.TEMPERATURE_MAX ||
                vitals.getTemperature() < VitalsThreshold.TEMPERATURE_MIN) {
            triggerAlert(vitals.getPatientID(),
                    "Critical Alert: Abnormal Body Temperature",
                    "The patient's body temperature is critical: " + vitals.getTemperature() + "°C. Please review the situation urgently.");
            isCritical = true;
        }

        // Check systolic blood pressure
        if (vitals.getBloodPressureSystolic() > VitalsThreshold.SYSTOLIC_BP_MAX ||
                vitals.getBloodPressureSystolic() < VitalsThreshold.SYSTOLIC_BP_MIN) {
            triggerAlert(vitals.getPatientID(),
                    "Critical Alert: Systolic Blood Pressure Out of Range",
                    "The patient's systolic blood pressure is critical: " + vitals.getBloodPressureSystolic() + " mmHg. Immediate review recommended.");
            isCritical = true;
        }

        // Check diastolic blood pressure
        if (vitals.getBloodPressureDiastolic() > VitalsThreshold.DIASTOLIC_BP_MAX ||
                vitals.getBloodPressureDiastolic() < VitalsThreshold.DIASTOLIC_BP_MIN) {
            triggerAlert(vitals.getPatientID(),
                    subject,
                    "The patient's diastolic blood pressure is critical: " + vitals.getBloodPressureDiastolic() + " mmHg. Immediate review recommended.");
            isCritical = true;
        }

        // Check oxygen saturation
        if (vitals.getOxygenLevel() < VitalsThreshold.OXYGEN_LEVEL_MIN) {
            triggerAlert(vitals.getPatientID(),
                    "Critical Alert: Low Oxygen Saturation Detected",
                    "The patient's oxygen level is dangerously low: " + vitals.getOxygenLevel() + "%. Immediate medical intervention advised.");
            isCritical = true;
        }

        return isCritical;
    }
    public void sendEmailToEmergencyContact(Patient p, String subject, String message) {
        ns.sendEmail(p.getEmergencyEmail(), subject, message);
    }

    private void triggerAlert(long patientId, String subject, String message) {
        // Fetch patient and their assigned doctor
        Patient patient = Objects.requireNonNull(Helper.fetchPatient(patientId));
        long doctorId = patient.getAssignedTo();
        Doctor doctor = Objects.requireNonNull(Helper.fetchDoctor(doctorId));

        // Notify the doctor via email
        ns.sendEmail(doctor.getEmail(), subject, message);
    }
}
