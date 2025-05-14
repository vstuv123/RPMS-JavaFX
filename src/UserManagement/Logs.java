package UserManagement;

public class Logs {

    private String userId;        // Holds the ID of the user performing the action
    private String actionType;    // Type of the action performed (e.g., "Login", "Logout", etc.)
    private String desc;          // Description of the action performed

    // Constructor to initialize the Logs object
    public Logs(String userId, String actionType, String desc) {
        this.userId = userId;
        this.actionType = actionType;
        this.desc = desc;
    }

    // Getter for userId
    public String getUserId() {
        return userId;  // Return the userId associated with the log
    }

    // Getter for actionType
    public String getActionType() {
        return actionType;  // Return the actionType of the log (e.g., "Login")
    }

    // Getter for desc
    public String getDesc() {
        return desc;  // Return the description of the action
    }
}

