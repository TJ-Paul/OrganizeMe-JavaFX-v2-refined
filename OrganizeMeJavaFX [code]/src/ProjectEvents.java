public interface ProjectEvents {
    void onSystemMessage(String message);
    void onChatMessage(String message);
    void onUsersUpdated(String usersCsv);
    void onTaskAdded(String taskData);
    void onTaskCompleted(String taskData);
    void onTaskDeleted(String taskData);
    void onDisconnected(String reason);
}
