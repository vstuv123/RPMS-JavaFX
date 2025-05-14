package resources;

import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private static Properties properties = new Properties();  // Properties object to hold key-value pairs from the config file

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("resources/config.properties")) {
            // Try to load the config file as an input stream
            if (input == null) {
                // If the file is not found, throw an exception with an appropriate message
                throw new RuntimeException("Sorry, unable to find config.properties");
            }
            properties.load(input);  // Load the properties from the input stream

        } catch (Exception e) {
            // Catch any exceptions during the loading process and throw a runtime exception with the error message
            throw new RuntimeException("Failed to load config.properties", e);
        }
    }

    // Method to fetch a property value by key
    public static String get(String key) {
        return properties.getProperty(key);  // Return the value associated with the provided key
    }
}
