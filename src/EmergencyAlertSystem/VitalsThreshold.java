package EmergencyAlertSystem;

public final class VitalsThreshold {

    // Maximum and minimum heart rate limits (in beats per minute)
    public static final int HEART_RATE_MAX = 130; // Maximum safe heart rate
    public static final int HEART_RATE_MIN = 50;  // Minimum safe heart rate

    // Maximum and minimum body temperature limits (in degrees Fahrenheit)
    public static final double TEMPERATURE_MAX = 102.0; // Maximum safe body temperature
    public static final double TEMPERATURE_MIN = 95.0;  // Minimum safe body temperature

    // Maximum and minimum systolic blood pressure limits (in mmHg)
    public static final int SYSTOLIC_BP_MAX = 180; // Maximum systolic blood pressure
    public static final int SYSTOLIC_BP_MIN = 90;  // Minimum systolic blood pressure

    // Maximum and minimum diastolic blood pressure limits (in mmHg)
    public static final int DIASTOLIC_BP_MAX = 120; // Maximum diastolic blood pressure
    public static final int DIASTOLIC_BP_MIN = 60;  // Minimum diastolic blood pressure

    // Minimum oxygen saturation level (percentage)
    public static final int OXYGEN_LEVEL_MIN = 90; // Minimum acceptable oxygen saturation level
}