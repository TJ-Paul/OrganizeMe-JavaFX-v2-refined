import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Supplier;

public class ProjectViewHelper {

    private final VBox taskArea;
    private final VBox chatArea;
    private final ScrollPane scrollPane;
    private final ScrollPane chatScrollPane;
    private final ListView<String> userList;
    private final Label userCount;

    // We don't store direct username/client, we use suppliers so they always reflect latest values
    private final Supplier<String> usernameSupplier;
    private final Supplier<ProjectClient> clientSupplier;

    public ProjectViewHelper(
            VBox taskArea,
            VBox chatArea,
            ScrollPane scrollPane,
            ScrollPane chatScrollPane,
            ListView<String> userList,
            Label userCount,
            Supplier<String> usernameSupplier,
            Supplier<ProjectClient> clientSupplier
    ) {
        this.taskArea = taskArea;
        this.chatArea = chatArea;
        this.scrollPane = scrollPane;
        this.chatScrollPane = chatScrollPane;
        this.userList = userList;
        this.userCount = userCount;
        this.usernameSupplier = usernameSupplier;
        this.clientSupplier = clientSupplier;

        // Auto-scroll when content grows
        this.taskArea.heightProperty().addListener((obs, ov, nv) -> this.scrollPane.setVvalue(1.0));
        this.chatArea.heightProperty().addListener((obs, ov, nv) -> this.chatScrollPane.setVvalue(1.0));
    }

    // --------- Public methods used by controller / events ---------

    public void addSystemMessage(String message) {
        HBox messageBox = createChatMessageBox(message, "system-message", Pos.CENTER);
        chatArea.getChildren().add(messageBox);
    }

    public void addChatMessage(String message) {
        String username = usernameSupplier.get();
        String[] parts = message.split(": ", 2);

        if (parts[0].equals(username)) {
            HBox messageBox = createChatMessageBox(message, "user-message", Pos.CENTER_RIGHT);
            chatArea.getChildren().add(messageBox);
        } else {
            HBox messageBox = createChatMessageBox(message, "contact-message", Pos.CENTER_LEFT);
            chatArea.getChildren().add(messageBox);
        }
    }

    public void updateUserList(String userListString) {
        userList.getItems().clear();
        if (!userListString.isEmpty()) {
            String[] users = userListString.split(",");
            for (String user : users) {
                userList.getItems().add(user);
            }
            userCount.setText(users.length + " users online");
        } else {
            userCount.setText("0 users online");
        }
    }

    public void handleTaskAdded(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 5) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String description = parts[2];
            String assignedBy = parts[3];
            String status = parts[4];
            String completedBy = parts.length > 5 ? parts[5] : "";

            addTaskToUI(taskId, title, description, assignedBy, status, completedBy);
        }
    }

    public void handleTaskCompleted(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 3) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String completedBy = parts[2];

            addSystemMessage("Task completed: \"" + title + "\" by " + completedBy);
            updateTaskStatus(taskId, "COMPLETED", completedBy);
        }
    }

    public void handleTaskDeleted(String taskData) {
        String[] parts = taskData.split("\\|");
        if (parts.length >= 3) {
            int taskId = Integer.parseInt(parts[0]);
            String title = parts[1];
            String deletedBy = parts[2];

            addSystemMessage("Task deleted: \"" + title + "\" by " + deletedBy);
            removeTaskFromUI(taskId);
        }
    }

    // --------- Internal helpers (UI only) ---------

    private void addTaskToUI(
        int taskId, 
        String title, 
        String description, 
        String assignedBy,
        String status, 
        String completedBy
    ) {

        VBox taskBox = new VBox(5);
        taskBox.setUserData(taskId);
        taskBox.setPadding(new Insets(10));
        taskBox.getStyleClass().add(status.equals("COMPLETED") ? "task-completed" : "task-pending");

        Label titleLabel = new Label("Task #" + taskId + ": " + title);
        titleLabel.getStyleClass().add("task-title");

        Label descriptionLabel = new Label(description);
        descriptionLabel.setWrapText(true);
        descriptionLabel.getStyleClass().add("task-description");

        Label assignedLabel = new Label("Assigned by: " + assignedBy);
        assignedLabel.getStyleClass().add("task-assigned");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        if (status.equals("PENDING")) {
            Button completeButton = new Button("Mark Complete");
            completeButton.getStyleClass().add("complete-button");
            completeButton.setOnAction(e -> {
                ProjectClient c = clientSupplier.get();
                if (c != null) c.completeTask(taskId);
            });

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Are you sure you want to delete this task?");
                confirmAlert.setContentText("Task: " + title);

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    ProjectClient c = clientSupplier.get();
                    if (c != null) c.deleteTask(taskId);
                }
            });

            buttonBox.getChildren().addAll(completeButton, deleteButton);
        } else {
            Label completedLabel = new Label("✓ Completed by " + completedBy);
            completedLabel.getStyleClass().add("completed-label");

            Button deleteButton = new Button("Delete");
            deleteButton.getStyleClass().add("delete-button");
            deleteButton.setOnAction(e -> {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Delete Task");
                confirmAlert.setHeaderText("Are you sure you want to delete this task?");
                confirmAlert.setContentText("Task: " + title);

                Optional<ButtonType> result = confirmAlert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    ProjectClient c = clientSupplier.get();
                    if (c != null) c.deleteTask(taskId);
                }
            });

            buttonBox.getChildren().addAll(completedLabel, deleteButton);
        }

        taskBox.getChildren().addAll(titleLabel, descriptionLabel, assignedLabel, buttonBox);
        taskArea.getChildren().add(taskBox);
    }

    private void updateTaskStatus(int taskId, String status, String completedBy) {
        for (int i = 0; i < taskArea.getChildren().size(); i++) {
            if (taskArea.getChildren().get(i) instanceof VBox) {
                VBox taskBox = (VBox) taskArea.getChildren().get(i);
                if (taskBox.getUserData() != null && taskBox.getUserData().equals(taskId)) {
                    taskBox.getStyleClass().removeAll("task-pending");
                    taskBox.getStyleClass().add("task-completed");

                    for (int j = 0; j < taskBox.getChildren().size(); j++) {
                        if (taskBox.getChildren().get(j) instanceof HBox) {
                            HBox buttonBox = (HBox) taskBox.getChildren().get(j);
                            buttonBox.getChildren().clear();

                            Label completedLabel = new Label("✓ Completed by " + completedBy);
                            completedLabel.getStyleClass().add("completed-label");
                            buttonBox.getChildren().add(completedLabel);
                        }
                    }
                    break;
                }
            }
        }
    }

    private void removeTaskFromUI(int taskId) {
        taskArea.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                VBox taskBox = (VBox) node;
                return taskBox.getUserData() != null && taskBox.getUserData().equals(taskId);
            }
            return false;
        });
    }

    private HBox createChatMessageBox(String message, String styleClass, Pos alignment) {
        HBox messageBox = new HBox();
        messageBox.setAlignment(alignment);
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        VBox messageBubble = new VBox();
        messageBubble.getStyleClass().add(styleClass);
        messageBubble.setPadding(new Insets(8, 12, 8, 12));
        messageBubble.setMaxWidth(300);

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.getStyleClass().add("message-text");

        Label timeLabel = new Label(getCurrentTime());
        timeLabel.getStyleClass().add("time-label");

        messageBubble.getChildren().addAll(messageLabel, timeLabel);
        messageBox.getChildren().add(messageBubble);

        return messageBox;
    }

    private String getCurrentTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}
