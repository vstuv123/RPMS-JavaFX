 1. Introduction
RPMS (Remote Patient Monitoring System) is a desktop-based healthcare application built using Java and JavaFX. It allows healthcare providers to monitor patients remotely, track their medical reports, assign doctors, and manage appointments or emergencies — all through a smart and secure system.

This system is ideal for clinics, hospitals, or health tech startups wanting to digitize patient care workflows. It supports real-time data management, report generation, and communication with patients or staff.

💻 2. What You Need to Run This Project
Before you run this project, install the following things on your computer:

Java JDK 21 (64-bit) – Needed to run Java programs

JavaFX 21 SDK – Used to design the UI of the application

MySQL Server – To store all the data like patients, reports, doctors, etc.

Internet – Required for sending emails or SMS alerts (Twilio, SMTP, etc.)

🔗 3. Download Links
🔹 Java JDK 21
[Installer: jdk-21.0.6_windows-x64_bin.exe](https://download.oracle.com/java/21/archive/jdk-21.0.6_windows-x64_bin.exe)

[ZIP Archive: jdk-21.0.6_windows-x64_bin.zip](https://download.oracle.com/java/21/archive/jdk-21.0.6_windows-x64_bin.zip)

🔹 JavaFX 21 SDK
[SDK (.zip): openjfx-21.0.2 SDK](https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-sdk.zip)

https://download2.gluonhq.com/openjfx/21.0.2/openjfx-21.0.2_windows-x64_bin-jmods.zip

📦 4. External Libraries (JAR Files)
Add the following JAR files to your project’s lib or classpath directory:

activation-1.1.1.jar

commons-codec-1.15.jar

commons-logging-1.2.jar

httpclient-4.5.14.jar

httpcore-4.4.16.jar

itextpdf-5.5.13.3.jar

jackson-annotations-2.15.3.jar

jackson-core-2.15.3.jar

jackson-databind-2.15.3.jar

jackson-datatype-jsr310-2.15.3.jar

jasperreports-6.20.0.jar

javax.mail-1.6.2.jar

jaxb-api-2.3.1.jar

jaxb-core-2.3.0.1.jar

jaxb-impl-2.3.3.jar

jbcrypt-0.4.jar

mysql-connector-j-8.0.33.jar

slf4j-api-1.7.36.jar

slf4j-simple-1.7.36.jar

twilio-8.31.1.jar

These files help with database connectivity, PDF generation, email sending, API integration, etc.

▶️ 5. How to Run the Project
Install Java JDK 21
→ Use the link above and install it like a normal Windows program.

Extract JavaFX SDK
→ Place it anywhere like your Desktop, and remember the folder path.

Add all JAR files
→ Put all the .jar files in a folder called lib. You will add these to your IDE's build path.

Open the project in your IDE (like IntelliJ IDEA or Eclipse).

Go to project settings
→ Add JavaFX SDK and all .jar files to your project dependencies.

Set VM options to run JavaFX properly
Example:

cpp
Copy
Edit
--module-path "path_to_javafx/lib" --add-modules javafx.controls,javafx.fxml
Run the main class
This will launch the RPMS interface.

Login as a test user or admin
Credentials are typically set in the database for testing.

Start exploring modules
Add patients, assign doctors, submit reports, or send alerts.

📹 6. Project Demo Video
You can watch a full video demonstration of the RPMS system here:

https://drive.google.com/file/d/1gvnKflmfWinNNumKuhFguOiv-_lyytEV/view?usp=drive_link

🛠 7. Support
If anything breaks or doesn’t run properly:

Check Java and JavaFX installation paths.

Make sure your database is up and running.

See the project video for guidance.

Or contact the developer or supervisor if it's part of a course.
