import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javafx.event.ActionEvent;
import javafx.geometry.Insets;

public class todoController {

    @FXML private TextField taskInput;
    @FXML private DatePicker dueDatePicker;
    @FXML private TextField dueTimePicker;
    @FXML private ComboBox<TodoTask.Priority> priorityComboBox;
    @FXML private ComboBox<TodoTask.RecurringType> recurringComboBox;
    @FXML private VBox taskList;
    @FXML private Button addTask;
    @FXML private Button clear;
    @FXML private Button clearCompleted;
    
    // Filter and Search controls
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<TodoTask.Priority> priorityFilter;
    @FXML private ComboBox<String> sortComboBox;
    @FXML private CheckBox showOverdueOnly;
    @FXML private Button refreshButton;
    @FXML private Button toggleFiltersButton;
    @FXML private VBox filtersContainer;
    
    // Statistics labels
    @FXML private Label totalTasksLabel;
    @FXML private Label completedTasksLabel;
    @FXML private Label pendingTasksLabel;
    @FXML private Label overdueTasksLabel;

    private String currentUsername;
    private String category = null;
    private List<TodoTask> allTasks = new ArrayList<>();
    private boolean filtersVisible = false;

    // Predefined categories - you can modify this list as needed
    private final List<String> AVAILABLE_CATEGORIES = Arrays.asList(
        "Personal", "Work", "Shopping", "Health", "Education", "Finance", 
        "Travel", "Home", "Projects", "Goals", "Urgent", "Ideas"
    );

    @FXML
    public void initialize() {
        System.out.println("todoController initialize() called");
        try {
            setupComboBoxes();
            setupSearchAndFilter();
            setupToggleButton();
            System.out.println("todoController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error during todoController initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupComboBoxes() {
        try {
            // Priority ComboBox
            priorityComboBox.setItems(FXCollections.observableArrayList(TodoTask.Priority.values()));
            priorityComboBox.setValue(TodoTask.Priority.MEDIUM);
            
            priorityFilter.setItems(FXCollections.observableArrayList(TodoTask.Priority.values()));
            ObservableList<TodoTask.Priority> priorityFilterItems = FXCollections.observableArrayList(TodoTask.Priority.values());
            priorityFilterItems.add(0, null); // Add "All" option
            priorityFilter.setItems(priorityFilterItems);
            
            // Recurring ComboBox
            recurringComboBox.setItems(FXCollections.observableArrayList(TodoTask.RecurringType.values()));
            recurringComboBox.setValue(TodoTask.RecurringType.NONE);
            
            // Status Filter
            statusFilter.setItems(FXCollections.observableArrayList("All", "Completed", "Pending", "Overdue"));
            statusFilter.setValue("All");
            
            // Sort ComboBox
            sortComboBox.setItems(FXCollections.observableArrayList(
                "Date Created", "Due Date", "Priority", "Title", "Status"
            ));
            sortComboBox.setValue("Date Created");
            
            // Set time picker prompt
            dueTimePicker.setPromptText("HH:MM");
            
            System.out.println("ComboBoxes setup completed");
        } catch (Exception e) {
            System.err.println("Error setting up combo boxes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupSearchAndFilter() {
        try {
            // Add listeners for real-time filtering
            if (searchField != null) {
                searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            }
            if (statusFilter != null) {
                statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            }
            if (priorityFilter != null) {
                priorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            }
            if (sortComboBox != null) {
                sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
            }
            System.out.println("Search and filter setup completed");
        } catch (Exception e) {
            System.err.println("Error setting up search and filter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupToggleButton() {
        if (toggleFiltersButton != null) {
            toggleFiltersButton.setText("⚙ Filters & Search");
        }
    }

    @FXML
    void toggleFilters() {
        filtersVisible = !filtersVisible;
        
        if (filtersContainer == null) return;
        
        if (filtersVisible) {
            // Show filters
            filtersContainer.setVisible(true);
            filtersContainer.setManaged(true);
            if (toggleFiltersButton != null) {
                toggleFiltersButton.setText("⚙ Hide Filters");
            }
            
            // Smooth animation
            filtersContainer.setOpacity(0);
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(200), 
                    new KeyValue(filtersContainer.opacityProperty(), 1.0))
            );
            timeline.play();
        } else {
            // Hide filters
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(150), 
                    new KeyValue(filtersContainer.opacityProperty(), 0.0))
            );
            timeline.setOnFinished(e -> {
                filtersContainer.setVisible(false);
                filtersContainer.setManaged(false);
            });
            timeline.play();
            if (toggleFiltersButton != null) {
                toggleFiltersButton.setText("⚙ Filters & Search");
            }
        }
    }

    private void shakeNode(TextField node) {
        if (node == null) return;
        TranslateTransition shake = new TranslateTransition(Duration.millis(50), node);
        shake.setFromX(0);
        shake.setByX(4);  // move right by 4 pixels
        shake.setCycleCount(6); // 3 full shakes (left and right)
        shake.setAutoReverse(true);
        shake.play();
    }

    @FXML
    void handleAddTask(ActionEvent event) {
        try {
            String title = taskInput.getText().trim();
            if (title.isEmpty()) {
                // Add subtle shake animation or focus
                shakeNode(taskInput);
                taskInput.requestFocus();
                return;
            }

            LocalDate dueDate = dueDatePicker.getValue();
            LocalTime dueTime = null;
            
            // Parse time if provided
            String timeText = dueTimePicker.getText().trim();
            if (!timeText.isEmpty()) {
                try {
                    dueTime = LocalTime.parse(timeText, DateTimeFormatter.ofPattern("HH:mm"));
                } catch (DateTimeParseException e) {
                    showAlert("Invalid Time", "Please enter time in HH:MM format (24-hour)", Alert.AlertType.WARNING);
                    return;
                }
            }

            TodoTask.Priority priority = priorityComboBox.getValue();
            TodoTask.RecurringType recurring = recurringComboBox.getValue();

            TodoTask newTask = new TodoTask(title, dueDate, dueTime, priority, recurring);
            allTasks.add(newTask);
            
            saveTasksToFile();
            applyFilters();
            updateStatistics();
            clearInputFields();
        } catch (Exception e) {
            System.err.println("Error adding task: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void clearTasks(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Clear All Tasks");
            alert.setHeaderText("Are you sure?");
            alert.setContentText("This will permanently delete all tasks in this category.");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                allTasks.clear();
                if (taskList != null) {
                    taskList.getChildren().clear();
                }
                clearTasksFromFile();
                updateStatistics();
            }
        } catch (Exception e) {
            System.err.println("Error clearing tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void clearCompletedTasks(ActionEvent event) {
        try {
            allTasks.removeIf(TodoTask::isCompleted);
            saveTasksToFile();
            applyFilters();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Error clearing completed tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void applyFilters() {
        try {
            if (searchField == null || statusFilter == null || priorityFilter == null || 
                sortComboBox == null || showOverdueOnly == null) {
                System.err.println("Filter controls not properly initialized");
                return;
            }

            String searchText = searchField.getText().toLowerCase().trim();
            String statusFilterValue = statusFilter.getValue();
            TodoTask.Priority priorityFilterValue = priorityFilter.getValue();
            String sortBy = sortComboBox.getValue();
            boolean showOverdueOnlyValue = showOverdueOnly.isSelected();

            List<TodoTask> filteredTasks = allTasks.stream()
                .filter(task -> {
                    // Search filter
                    if (!searchText.isEmpty() && !task.getTitle().toLowerCase().contains(searchText)) {
                        return false;
                    }
                    
                    // Status filter
                    if (!"All".equals(statusFilterValue)) {
                        switch (statusFilterValue) {
                            case "Completed":
                                if (!task.isCompleted()) return false;
                                break;
                            case "Pending":
                                if (task.isCompleted()) return false;
                                break;
                            case "Overdue":
                                if (!task.isOverdue()) return false;
                                break;
                        }
                    }
                    
                    // Priority filter
                    if (priorityFilterValue != null && task.getPriority() != priorityFilterValue) {
                        return false;
                    }
                    
                    // Show overdue only
                    if (showOverdueOnlyValue && !task.isOverdue()) {
                        return false;
                    }
                    
                    return true;
                })
                .sorted(getComparator(sortBy))
                .collect(Collectors.toList());

            displayTasks(filteredTasks);
        } catch (Exception e) {
            System.err.println("Error applying filters: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    void refreshTasks() {
        try {
            processRecurringTasks();
            loadTasksFromFile();
            applyFilters();
            updateStatistics();
        } catch (Exception e) {
            System.err.println("Error refreshing tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processRecurringTasks() {
        try {
            List<TodoTask> newRecurringTasks = new ArrayList<>();
            LocalDate today = LocalDate.now();
            
            for (TodoTask task : allTasks) {
                if (task.isCompleted() && task.getRecurringType() != TodoTask.RecurringType.NONE) {
                    if (task.getDueDate() != null && !task.getDueDate().isAfter(today)) {
                        TodoTask nextTask = task.createNextRecurrence();
                        if (nextTask != null) {
                            newRecurringTasks.add(nextTask);
                        }
                    }
                }
            }
            
            allTasks.addAll(newRecurringTasks);
            if (!newRecurringTasks.isEmpty()) {
                saveTasksToFile();
            }
        } catch (Exception e) {
            System.err.println("Error processing recurring tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Comparator<TodoTask> getComparator(String sortBy) {
        return switch (sortBy) {
            case "Due Date" -> Comparator.comparing(TodoTask::getDueDate, Comparator.nullsLast(Comparator.naturalOrder()));
            case "Priority" -> Comparator.comparing(TodoTask::getPriority, Comparator.reverseOrder());
            case "Title" -> Comparator.comparing(TodoTask::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "Status" -> Comparator.comparing(TodoTask::isCompleted);
            default -> Comparator.comparing(TodoTask::getCreatedDate, Comparator.reverseOrder());
        };
    }

    private void displayTasks(List<TodoTask> tasks) {
        if (taskList == null) return;
        
        try {
            taskList.getChildren().clear();
            
            for (TodoTask task : tasks) {
                HBox taskItem = createTaskItem(task);
                taskList.getChildren().add(taskItem);
            }
        } catch (Exception e) {
            System.err.println("Error displaying tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private HBox createTaskItem(TodoTask task) {
        CheckBox checkBox = new CheckBox();
        TextField taskTitle = new TextField(task.getTitle());
        Label dueDateLabel = new Label(task.getFormattedDueDate());
        Label priorityLabel = new Label(task.getPriority().toString());
        Button editButton = new Button("Edit");
        Button switchButton = new Button("⇄");
        Button removeButton = new Button("×");

        // Styling
        taskTitle.getStyleClass().add(task.isCompleted() ? "task-completed" : "task");
        taskTitle.setEditable(!task.isCompleted());
        checkBox.setSelected(task.isCompleted());
        
        // Priority color
        priorityLabel.setStyle("-fx-background-color: " + task.getPriorityColor() + 
                             "; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 600;");
        
        // Overdue styling
        if (task.isOverdue() && !task.isCompleted()) {
            dueDateLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: 600;");
            taskTitle.setStyle(taskTitle.getStyle() + "; -fx-border-color: #ef4444; -fx-border-width: 1.5;");
        }
        
        // Recurring indicator
        if (task.getRecurringType() != TodoTask.RecurringType.NONE) {
            Label recurringLabel = new Label("↻");
            recurringLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #3b82f6; -fx-font-weight: 600;");
            recurringLabel.setTooltip(new Tooltip(task.getRecurringType().toString()));
            dueDateLabel.setText(dueDateLabel.getText() + " ↻");
        }

        // Style buttons
        editButton.getStyleClass().add("edit-button");
        switchButton.getStyleClass().add("switch-button");
        switchButton.setTooltip(new Tooltip("Move to another category"));
        removeButton.getStyleClass().add("remove");

        HBox taskBox = new HBox(12, checkBox, taskTitle, dueDateLabel, priorityLabel, editButton, switchButton, removeButton);
        taskBox.setPadding(new Insets(16));
        taskBox.getStyleClass().add("task-item");
        
        if (task.isCompleted()) {
            taskBox.getStyleClass().add("task-item-completed");
        }
        if (task.isOverdue() && !task.isCompleted()) {
            taskBox.getStyleClass().add("task-item-overdue");
        }
        
        // Set widths
        HBox.setHgrow(taskTitle, Priority.ALWAYS);
        dueDateLabel.setPrefWidth(120);
        priorityLabel.setPrefWidth(80);
        editButton.setPrefWidth(50);
        switchButton.setPrefWidth(30);
        removeButton.setPrefWidth(30);

        // Event handlers
        checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            task.setCompleted(isSelected);
            taskTitle.getStyleClass().removeAll("task", "task-completed");
            taskTitle.getStyleClass().add(isSelected ? "task-completed" : "task");
            taskTitle.setEditable(!isSelected);
            
            taskBox.getStyleClass().removeAll("task-item-completed");
            if (isSelected) {
                taskBox.getStyleClass().add("task-item-completed");
            }
            
            saveTasksToFile();
            updateStatistics();
        });

        taskTitle.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Focus lost
                task.setTitle(taskTitle.getText());
                saveTasksToFile();
            }
        });

        editButton.setOnAction(e -> openTaskEditDialog(task));
        
        switchButton.setOnAction(e -> openSwitchCategoryDialog(task));
        
        removeButton.setOnAction(e -> {
            allTasks.remove(task);
            saveTasksToFile();
            applyFilters();
            updateStatistics();
        });

        return taskBox;
    }

    private void openSwitchCategoryDialog(TodoTask task) {
        try {
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Switch Category");
            dialog.setHeaderText("Move task to another category");

            ButtonType switchButtonType = new ButtonType("Switch", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(switchButtonType, ButtonType.CANCEL);

            VBox content = new VBox(15);
            content.setPadding(new Insets(20));

            Label taskLabel = new Label("Task: " + task.getTitle());
            taskLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

            Label currentCategoryLabel = new Label("Current Category: " + (category != null ? category : "Unknown"));
            currentCategoryLabel.setStyle("-fx-text-fill: #666;");

            Label selectLabel = new Label("Select target category:");
            
            ComboBox<String> categoryComboBox = new ComboBox<>();
            
            // Get available categories excluding current category
            ObservableList<String> availableCategories = FXCollections.observableArrayList();
            for (String cat : AVAILABLE_CATEGORIES) {
                if (!cat.equals(category)) {
                    availableCategories.add(cat);
                }
            }
            categoryComboBox.setItems(availableCategories);
            
            // Option to create new category
            TextField newCategoryField = new TextField();
            newCategoryField.setPromptText("Or enter new category name");
            
            CheckBox createNewCheckBox = new CheckBox("Create new category");
            
            // Toggle between existing and new category
            createNewCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                categoryComboBox.setDisable(newVal);
                newCategoryField.setDisable(!newVal);
                if (newVal) {
                    newCategoryField.requestFocus();
                } else {
                    categoryComboBox.requestFocus();
                }
            });

            content.getChildren().addAll(taskLabel, currentCategoryLabel, selectLabel, 
                                       categoryComboBox, createNewCheckBox, newCategoryField);
            dialog.getDialogPane().setContent(content);

            // Set default selection
            if (!availableCategories.isEmpty()) {
                categoryComboBox.setValue(availableCategories.get(0));
            }

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == switchButtonType) {
                    if (createNewCheckBox.isSelected()) {
                        String newCategory = newCategoryField.getText().trim();
                        if (!newCategory.isEmpty()) {
                            return newCategory;
                        } else {
                            showAlert("Invalid Input", "Please enter a category name.", Alert.AlertType.WARNING);
                            return null;
                        }
                    } else {
                        return categoryComboBox.getValue();
                    }
                }
                return null;
            });

            dialog.showAndWait().ifPresent(targetCategory -> {
                if (targetCategory != null && !targetCategory.equals(category)) {
                    switchTaskToCategory(task, targetCategory);
                }
            });

        } catch (Exception e) {
            System.err.println("Error opening switch category dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void switchTaskToCategory(TodoTask task, String targetCategory) {
        try {
            // Remove task from current category
            allTasks.remove(task);
            saveTasksToFile();
            
            // Add task to target category
            addTaskToCategory(task, targetCategory);
            
            // Refresh current view
            applyFilters();
            updateStatistics();
            
            // Show confirmation
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Task Moved");
            alert.setHeaderText("Success!");
            alert.setContentText("Task \"" + task.getTitle() + "\" has been moved to category \"" + targetCategory + "\".");
            alert.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Error switching task category: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to move task to new category.", Alert.AlertType.ERROR);
        }
    }

    private void addTaskToCategory(TodoTask task, String targetCategory) {
        try {
            if (currentUsername == null) {
                System.err.println("Cannot add task to category: username is null");
                return;
            }

            // Read existing tasks from target category file
            List<TodoTask> targetCategoryTasks = new ArrayList<>();
            File targetFile = new File("tasks_" + currentUsername + "_" + targetCategory + ".txt");
            
            if (targetFile.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(targetFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        TodoTask existingTask = TodoTask.fromFileString(line);
                        if (existingTask != null) {
                            targetCategoryTasks.add(existingTask);
                        }
                    }
                }
            }
            
            // Add the switched task
            targetCategoryTasks.add(task);
            
            // Save all tasks to target category file
            try (PrintWriter writer = new PrintWriter(targetFile)) {
                for (TodoTask t : targetCategoryTasks) {
                    writer.println(t.toFileString());
                }
            }
            
            System.out.println("Task moved to category: " + targetCategory);
            
        } catch (IOException e) {
            System.err.println("Error adding task to target category: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to move task to new category", e);
        }
    }

    private void openTaskEditDialog(TodoTask task) {
        try {
            Dialog<TodoTask> dialog = new Dialog<>();
            dialog.setTitle("Edit Task");
            dialog.setHeaderText("Edit task details");

            ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleField = new TextField(task.getTitle());
            DatePicker dateField = new DatePicker(task.getDueDate());
            TextField timeField = new TextField(task.getDueTime() != null ? 
                task.getDueTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "");
            ComboBox<TodoTask.Priority> priorityField = new ComboBox<>(
                FXCollections.observableArrayList(TodoTask.Priority.values()));
            priorityField.setValue(task.getPriority());
            ComboBox<TodoTask.RecurringType> recurringField = new ComboBox<>(
                FXCollections.observableArrayList(TodoTask.RecurringType.values()));
            recurringField.setValue(task.getRecurringType());

            grid.add(new Label("Title:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("Due Date:"), 0, 1);
            grid.add(dateField, 1, 1);
            grid.add(new Label("Due Time:"), 0, 2);
            grid.add(timeField, 1, 2);
            grid.add(new Label("Priority:"), 0, 3);
            grid.add(priorityField, 1, 3);
            grid.add(new Label("Recurring:"), 0, 4);
            grid.add(recurringField, 1, 4);

            dialog.getDialogPane().setContent(grid);

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    task.setTitle(titleField.getText());
                    task.setDueDate(dateField.getValue());
                    
                    try {
                        if (!timeField.getText().trim().isEmpty()) {
                            task.setDueTime(LocalTime.parse(timeField.getText(), 
                                DateTimeFormatter.ofPattern("HH:mm")));
                        } else {
                            task.setDueTime(null);
                        }
                    } catch (DateTimeParseException e) {
                        showAlert("Invalid Time", "Please enter time in HH:MM format", Alert.AlertType.WARNING);
                        return null;
                    }
                    
                    task.setPriority(priorityField.getValue());
                    task.setRecurringType(recurringField.getValue());
                    return task;
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                saveTasksToFile();
                applyFilters();
                updateStatistics();
            });
        } catch (Exception e) {
            System.err.println("Error opening task edit dialog: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateStatistics() {
        try {
            int total = allTasks.size();
            int completed = (int) allTasks.stream().filter(TodoTask::isCompleted).count();
            int pending = total - completed;
            int overdue = (int) allTasks.stream().filter(TodoTask::isOverdue).count();

            if (totalTasksLabel != null) totalTasksLabel.setText("Total: " + total);
            if (completedTasksLabel != null) completedTasksLabel.setText("Completed: " + completed);
            if (pendingTasksLabel != null) pendingTasksLabel.setText("Pending: " + pending);
            if (overdueTasksLabel != null) overdueTasksLabel.setText("Overdue: " + overdue);
        } catch (Exception e) {
            System.err.println("Error updating statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearInputFields() {
        try {
            if (taskInput != null) taskInput.clear();
            if (dueDatePicker != null) dueDatePicker.setValue(null);
            if (dueTimePicker != null) dueTimePicker.clear();
            if (priorityComboBox != null) priorityComboBox.setValue(TodoTask.Priority.MEDIUM);
            if (recurringComboBox != null) recurringComboBox.setValue(TodoTask.RecurringType.NONE);
            if (taskInput != null) taskInput.requestFocus(); // Keep focus on input for easy task entry
        } catch (Exception e) {
            System.err.println("Error clearing input fields: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setUsername(String username) {
        this.currentUsername = username;
        System.out.println("Username set: " + username);
        maybeLoadTasks();
    }

    public void setCategory(String category) {
        this.category = category;
        System.out.println("Category set: " + category);
        maybeLoadTasks();
    }

    private void maybeLoadTasks() {
        if (currentUsername != null && category != null) {
            System.out.println("Loading tasks for user: " + currentUsername + ", category: " + category);
            loadTasksFromFile();
            applyFilters();
            updateStatistics();
        }
    }

    private void saveTasksToFile() {
        if (currentUsername == null || category == null) {
            System.err.println("Cannot save tasks: username or category is null");
            return;
        }

        File file = new File("tasks_" + currentUsername + "_" + category + ".txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            for (TodoTask task : allTasks) {
                writer.println(task.toFileString());
            }
            System.out.println("Tasks saved to file: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTasksFromFile() {
        if (currentUsername == null || category == null) {
            System.err.println("Cannot load tasks: username or category is null");
            return;
        }
        
        allTasks.clear();
        File file = new File("tasks_" + currentUsername + "_" + category + ".txt");
        if (!file.exists()) {
            System.out.println("Tasks file does not exist: " + file.getName());
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                TodoTask task = TodoTask.fromFileString(line);
                if (task != null) {
                    allTasks.add(task);
                } else {
                    System.err.println("Failed to parse task from line " + lineCount + ": " + line);
                }
            }
            System.out.println("Loaded " + allTasks.size() + " tasks from file: " + file.getName());
        } catch (IOException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void clearTasksFromFile() {
        if (currentUsername == null || category == null) return;
        
        File file = new File("tasks_" + currentUsername + "_" + category + ".txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.print("");
        } catch (IOException e) {
            System.err.println("Error clearing tasks file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Legacy methods for backward compatibility (if needed)
    @FXML
    public void logoutbtn(ActionEvent event) throws IOException {
        Session.clear();
        App.changeScene("fxmlFiles/login.fxml");
    }

    @FXML
    void backbtn(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/sidebar.fxml");
    }

    @FXML
    public void completeTask(ActionEvent event) {
        // Your existing task completion logic here
        // After a task is marked as complete, call:
        // StreakController.recordActivity();
        
        // Show a confirmation with streak info
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Task Completed!");
        alert.setHeaderText("Great job! 🎉");
        alert.setContentText("Task completed successfully!\n🔥 Activity recorded for your streak!");
        alert.showAndWait();
        
        // Continue with your existing task completion logic
    }

    @FXML
    public void openStreakPage(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/streak.fxml");
    }
}


class TodoTask {
    
    public enum Priority {
        LOW("#10b981"),    // Green
        MEDIUM("#f59e0b"), // Yellow
        HIGH("#ef4444");   // Red
        
        private final String color;
        
        Priority(String color) {
            this.color = color;
        }
        
        public String getColor() {
            return color;
        }
    }
    
    public enum RecurringType {
        NONE, DAILY, WEEKLY, MONTHLY
    }
    
    private String title;
    private LocalDate dueDate;
    private LocalTime dueTime;
    private Priority priority;
    private RecurringType recurringType;
    private boolean completed;
    private LocalDateTime createdDate;
    
    // Constructor
    public TodoTask(String title, LocalDate dueDate, LocalTime dueTime, Priority priority, RecurringType recurringType) {
        this.title = title;
        this.dueDate = dueDate;
        this.dueTime = dueTime;
        this.priority = priority != null ? priority : Priority.MEDIUM;
        this.recurringType = recurringType != null ? recurringType : RecurringType.NONE;
        this.completed = false;
        this.createdDate = LocalDateTime.now();
    }
    
    // Getters
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalTime getDueTime() { return dueTime; }
    public Priority getPriority() { return priority; }
    public RecurringType getRecurringType() { return recurringType; }
    public boolean isCompleted() { return completed; }
    public LocalDateTime getCreatedDate() { return createdDate; }
    
    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public void setDueTime(LocalTime dueTime) { this.dueTime = dueTime; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setRecurringType(RecurringType recurringType) { this.recurringType = recurringType; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    
    // Utility methods
    public String getPriorityColor() {
        return priority.getColor();
    }
    
    public boolean isOverdue() {
        if (completed || dueDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            return true;
        }
        if (dueDate.equals(today) && dueTime != null) {
            return LocalTime.now().isAfter(dueTime);
        }
        return false;
    }
    
    public String getFormattedDueDate() {
        if (dueDate == null) {
            return "No due date";
        }
        
        StringBuilder formatted = new StringBuilder();
        formatted.append(dueDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));
        
        if (dueTime != null) {
            formatted.append(" at ").append(dueTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        }
        
        return formatted.toString();
    }
    
    public TodoTask createNextRecurrence() {
        if (recurringType == RecurringType.NONE || dueDate == null) {
            return null;
        }
        
        LocalDate nextDueDate = switch (recurringType) {
            case DAILY -> dueDate.plusDays(1);
            case WEEKLY -> dueDate.plusWeeks(1);
            case MONTHLY -> dueDate.plusMonths(1);
            default -> null;
        };
        
        if (nextDueDate != null) {
            return new TodoTask(title, nextDueDate, dueTime, priority, recurringType);
        }
        
        return null;
    }
    
    // File serialization methods
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(title).append("|");
        sb.append(dueDate != null ? dueDate.toString() : "").append("|");
        sb.append(dueTime != null ? dueTime.toString() : "").append("|");
        sb.append(priority.name()).append("|");
        sb.append(recurringType.name()).append("|");
        sb.append(completed).append("|");
        sb.append(createdDate.toString());
        return sb.toString();
    }
    
    public static TodoTask fromFileString(String fileString) {
        try {
            String[] parts = fileString.split("\\|");
            if (parts.length < 7) {
                System.err.println("Invalid task format: " + fileString);
                return null;
            }
            
            String title = parts[0];
            LocalDate dueDate = parts[1].isEmpty() ? null : LocalDate.parse(parts[1]);
            LocalTime dueTime = parts[2].isEmpty() ? null : LocalTime.parse(parts[2]);
            Priority priority = Priority.valueOf(parts[3]);
            RecurringType recurringType = RecurringType.valueOf(parts[4]);
            boolean completed = Boolean.parseBoolean(parts[5]);
            LocalDateTime createdDate = LocalDateTime.parse(parts[6]);
            
            TodoTask task = new TodoTask(title, dueDate, dueTime, priority, recurringType);
            task.setCompleted(completed);
            task.createdDate = createdDate;
            
            return task;
        } catch (Exception e) {
            System.err.println("Error parsing task from string: " + fileString);
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public String toString() {
        return "TodoTask{" +
                "title='" + title + '\'' +
                ", dueDate=" + dueDate +
                ", dueTime=" + dueTime +
                ", priority=" + priority +
                ", recurringType=" + recurringType +
                ", completed=" + completed +
                ", createdDate=" + createdDate +
                '}';
    }
}