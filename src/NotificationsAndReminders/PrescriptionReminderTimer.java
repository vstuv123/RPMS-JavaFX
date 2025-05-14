package NotificationsAndReminders;

import java.util.Timer;
import java.util.TimerTask;

public class PrescriptionReminderTimer {
    private static boolean isRunning = false; // Ensures the timer starts only once

    public static void start() {
        if (isRunning) return; // Prevent multiple timer instances
        isRunning = true;

        Timer timer = new Timer(true); // Daemon timer runs in background
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                // Sends prescription reminders to patients every 2 hours
                new SendRemindersPatient().sendDailyDoseReminders();
            }
        }, 0, 2 * 60 * 60 * 1000); // Initial delay = 0ms, repeat every 2 hours
    }
}