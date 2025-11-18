import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class ProjectController {

    @FXML private VBox taskArea;
    @FXML private ScrollPane scrollPane;
    @FXML private TextField taskTitleInput;
    @FXML private TextArea taskDescriptionInput;
    @FXML private Button addTaskButton;
    @FXML private Label connectionStatus;
    @FXML private ListView<String> userList;
    @FXML private Label userCount;
    @FXML private TextField messageInput;
    @FXML private Button sendMessageButton;
    @FXML private VBox chatArea;
    @FXML private ScrollPane chatScrollPane;

    private ProjectClient client;
    private String username;
    private Stage primaryStage;
    private final BooleanProperty connected = new SimpleBooleanProperty(false);

    private ProjectViewHelper viewHelper;

    public void initialize() {
        // helper needs username + client suppliers
        Supplier<String> usernameSupplier = () -> username;
        Supplier<ProjectClient> clientSupplier = () -> client;

        viewHelper = new ProjectViewHelper(
            taskArea,
            chatArea,
            scrollPane,
            chatScrollPane,
            userList,
            userCount,
            usernameSupplier,
            clientSupplier
        );

        // Reactive enable/disable: driven by 'connected' + field emptiness
        BooleanBinding disconnected = connected.not();
        BooleanBinding titleEmpty = taskTitleInput.textProperty().isEmpty();
        BooleanBinding messageEmpty = messageInput.textProperty().isEmpty();

        taskTitleInput.disableProperty().bind(disconnected);
        taskDescriptionInput.disableProperty().bind(disconnected);
        messageInput.disableProperty().bind(disconnected);
        addTaskButton.disableProperty().bind(disconnected.or(titleEmpty));
        sendMessageButton.disableProperty().bind(disconnected.or(messageEmpty));

        // Enter-key behavior
        taskTitleInput.setOnAction(e -> handleAddTask());
        messageInput.setOnAction(e -> handleSendMessage());

        connectionStatus.setText("Disconnected");
        connectionStatus.getStyleClass().add("status-disconnected");
    }

    public void setPrimaryStage(Stage stage) {
        primaryStage = stage;
        primaryStage.setTitle("ula bula mula");
    }

    @FXML
    private void handleAddTask() {
        String title = taskTitleInput.getText().trim();
        String description = taskDescriptionInput.getText().trim();

        if (!title.isEmpty() && connected.get()) {
            client.addTask(title, description);
            taskTitleInput.clear();
            taskDescriptionInput.clear();
        } else if (title.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Task");
            alert.setHeaderText("Task title cannot be empty");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageInput.getText().trim();
        if (!message.isEmpty() && connected.get()) {
            client.sendChat(message);
            messageInput.clear();
        }
    }

    public void showConnectionDialog() {
        TextInputDialog serverDialog = new TextInputDialog("localhost");
        serverDialog.setTitle("Connect to Project Server");
        serverDialog.setHeaderText("Server Connection");
        serverDialog.setContentText("Enter server address:");

        Optional<String> serverResult = serverDialog.showAndWait();
        if (serverResult.isPresent()) {
            String serverAddress = serverResult.get();

            TextInputDialog usernameDialog = new TextInputDialog();
            usernameDialog.setTitle("Username");
            usernameDialog.setHeaderText("Enter Username");
            usernameDialog.setContentText("Choose your username:");

            Optional<String> usernameResult = usernameDialog.showAndWait();
            if (usernameResult.isPresent()) {
                String username = usernameResult.get().trim();
                if (!username.isEmpty()) {
                    connectToServer(serverAddress, username);
                } else {
                    showConnectionDialog(); // Retry if empty username
                }
            } else {
                primaryStage.close(); // Exit if user cancels
            }
        } else {
            primaryStage.close(); // Exit if user cancels
        }
    }

    private void connectToServer(String serverAddress, String username) {
        this.username = username;

        ProjectEvents events = new ProjectEvents() {
            @Override
            public void onSystemMessage(String message) {
                Platform.runLater(() -> 
                viewHelper.addSystemMessage(message));
            }

            @Override
            public void onChatMessage(String message) {
                Platform.runLater(() -> 
                viewHelper.addChatMessage(message));
            }

            @Override
            public void onUsersUpdated(String usersCsv) {
                Platform.runLater(() -> 
                viewHelper.updateUserList(usersCsv));
            }

            @Override
            public void onTaskAdded(String taskData) {
                Platform.runLater(() -> 
                viewHelper.handleTaskAdded(taskData));
            }

            @Override
            public void onTaskCompleted(String taskData) {
                Platform.runLater(() -> 
                viewHelper.handleTaskCompleted(taskData));
            }

            @Override
            public void onTaskDeleted(String taskData) {
                Platform.runLater(() -> 
                viewHelper.handleTaskDeleted(taskData));
            }

            @Override
            public void onDisconnected(String reason) {
                Platform.runLater(() -> {
                    viewHelper.addSystemMessage(reason);
                    connectionStatus.setText("Disconnected");
                    connectionStatus.getStyleClass().removeAll("status-connected");
                    connectionStatus.getStyleClass().add("status-disconnected");
                    connected.set(false);
                });
            }
        };

        client = new ProjectClient(serverAddress, 12345, username, events);

        try {
            client.connect();
            connected.set(true);

            connectionStatus.setText("Connected to " + serverAddress);
            connectionStatus.getStyleClass().removeAll("status-disconnected");
            connectionStatus.getStyleClass().add("status-connected");
            primaryStage.setTitle("Project Manager - " + username);

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Connection Error");
            alert.setHeaderText("Failed to connect to server");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
            showConnectionDialog(); // Retry
        }
    }

    public void disconnect() {
        connected.set(false);
        if (client != null) {
            client.disconnect();
        }
    }
}
