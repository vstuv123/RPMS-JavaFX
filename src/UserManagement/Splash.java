package UserManagement;

import NotificationsAndReminders.AppointmentReminderScheduler;
import NotificationsAndReminders.PrescriptionReminderTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class Splash extends Application {

    @Override
    public void start(Stage stage) {
        // Start the background tasks for appointment and prescription reminders
        PrescriptionReminderTimer.start();
        AppointmentReminderScheduler.startDailyTask();

        // Load the image to be displayed on the splash screen
        Image image = new Image("resources/icons/splash.png");
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(900); // Set the width of the image
        imageView.setFitHeight(600); // Set the height of the image

        // Create the root container to hold the image
        StackPane root = new StackPane(imageView);
        root.setScaleX(0.1); // Initial scale of the splash screen (very small)
        root.setScaleY(0.1); // Initial scale of the splash screen (very small)

        // Set up the scene for the splash screen
        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene); // Set the scene to the stage
        stage.initStyle(StageStyle.UNDECORATED); // Remove window borders for a clean look
        stage.centerOnScreen(); // Center the splash screen on the screen
        stage.show(); // Show the splash screen

        // Animate the scale of the splash screen from 0.1x to 1.0x
        ScaleTransition scale = new ScaleTransition(Duration.seconds(1.5), root);
        scale.setToX(1.0); // End scaling in X direction (normal size)
        scale.setToY(1.0); // End scaling in Y direction (normal size)

        // Once the scaling animation is finished, start a pause before moving to the login screen
        scale.setOnFinished(e -> {
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2.8));
            pause.setOnFinished(ev -> {
                // Close the splash screen and show the login window after a brief pause
                stage.close();
                try {
                    new LoginFX().start(new Stage()); // Launch the login window
                } catch (Exception ex) {
                    ex.printStackTrace(); // Print error if launching login screen fails
                }
            });
            pause.play(); // Start the pause transition
        });

        scale.play(); // Start the scale animation
    }
}
