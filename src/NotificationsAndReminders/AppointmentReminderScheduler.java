package NotificationsAndReminders;

import AppointmentScheduling.ViewAcceptedAppointment;

import java.time.*;
import java.util.concurrent.*;

public class AppointmentReminderScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();  // Executor for scheduling tasks

    // Method to start the daily reminder task that checks for appointments for the day
    public static void startDailyTask() {
        // Task to be run daily: retrieves today's appointments
        Runnable task = () -> {
            new ViewAcceptedAppointment("Doctor").getAppointmentsOfToday();  // Fetch appointments for today (Doctor as the example)
        };

        // Calculate the initial delay to run the task at 7:00 AM today or tomorrow
        long initialDelay = computeInitialDelay(7, 0);  // Target time: 7:00 AM
        long period = TimeUnit.DAYS.toSeconds(1);  // Interval for repeating the task (24 hours in seconds)

        // Schedule the task to run at 8:00 AM every day
        scheduler.scheduleAtFixedRate(task, initialDelay, period, TimeUnit.SECONDS);
    }

    // Method to compute the initial delay before the task is first run (from now until 8:00 AM)
    private static long computeInitialDelay(int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now();  // Current date and time
        // Set the target time to the desired hour and minute today (e.g., 8:00 AM)
        LocalDateTime nextRun = now.withHour(targetHour).withMinute(targetMinute).withSecond(0).withNano(0);

        // If the target time has already passed for today, schedule for the same time tomorrow
        if (now.compareTo(nextRun) >= 0) {
            nextRun = nextRun.plusDays(1);  // Move the time to tomorrow
        }

        // Calculate the delay in seconds from now to the next run time
        return Duration.between(now, nextRun).getSeconds();
    }
}