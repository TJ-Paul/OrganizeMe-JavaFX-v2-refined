import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class SidebarController {

    @FXML private VBox sidebar;
    @FXML private StackPane contentPane;
    @FXML private VBox categoriesContainer;
    @FXML private Button addCategoryBtn;
    @FXML private Button backToMenuBtn;
    
    private boolean sidebarVisible = true;
    private List<String> categories = new ArrayList<>();
    private Button selectedCategoryButton = null; // Track currently selected button
    private String selectedCategory = null; // Track currently selected category

    @FXML
    public void initialize() {
        loadCategoriesFromFile();
        refreshCategoriesDisplay();
    }

    @FXML
    private void toggleSidebar() {
        sidebarVisible = !sidebarVisible;
        sidebar.setManaged(sidebarVisible);
        sidebar.setVisible(sidebarVisible);
    }

    @FXML
    private void addCategory() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create a new category");
        dialog.setContentText("Category name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(categoryName -> {
            String trimmedName = categoryName.trim();
            if (!trimmedName.isEmpty() && !categories.contains(trimmedName.toLowerCase())) {
                categories.add(trimmedName.toLowerCase());
                saveCategoriestoFile();
                refreshCategoriesDisplay();
            } else if (categories.contains(trimmedName.toLowerCase())) {
                showAlert("Error", "Category already exists!", Alert.AlertType.ERROR);
            }
        });
    }

    @FXML
    public void userLogout(ActionEvent event) throws IOException{
        Session.clear();
        App.changeScene("fxmlFiles/login.fxml");
    }

    @FXML
    private Button viewStreakBtn;

    @FXML
    public void viewStreak(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/streak.fxml");
    }

    @FXML
    public void backToMenu(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/menu.fxml");
    }

    private void refreshCategoriesDisplay() {
        categoriesContainer.getChildren().clear();
        
        for (String category : categories) {
            Button categoryBtn = createCategoryButton(category);
            categoriesContainer.getChildren().add(categoryBtn);
            
            // Restore selection if this was the previously selected category
            if (category.equals(selectedCategory)) {
                setSelectedCategory(categoryBtn, category);
            }
        }
    }

    private Button createCategoryButton(String category) {
        Button btn = new Button(capitalizeFirst(category));
        btn.setPrefHeight(40.0);
        btn.setPrefWidth(167.0);
        btn.getStyleClass().add("category-button");
        
        // Left click to load category
        btn.setOnAction(e -> {
            setSelectedCategory(btn, category);
            loadCategoryContent(category);
        });
        
        // Right click context menu
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> renameCategory(category));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteCategory(category));
        
        contextMenu.getItems().addAll(renameItem, deleteItem);
        btn.setContextMenu(contextMenu);
        
        return btn;
    }

    private void setSelectedCategory(Button button, String category) {
        // Remove selected style from previously selected button
        if (selectedCategoryButton != null) {
            selectedCategoryButton.getStyleClass().remove("category-button-selected");
        }
        
        // new selected button and add selected style
        selectedCategoryButton = button;
        selectedCategory = category;
        button.getStyleClass().add("category-button-selected");
    }

    private void loadCategoryContent(String category) {
        try {
            loadContentWithCategory("fxmlFiles/todo.fxml", category);
        } catch (IOException e) {
            showAlert("Error", "Failed to load category content: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void renameCategory(String oldCategory) {
        TextInputDialog dialog = new TextInputDialog(capitalizeFirst(oldCategory));
        dialog.setTitle("Rename Category");
        dialog.setHeaderText("Rename category: " + capitalizeFirst(oldCategory));
        dialog.setContentText("New name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            String trimmedName = newName.trim().toLowerCase();
            if (!trimmedName.isEmpty() && !categories.contains(trimmedName)) {
                // Update categories list
                int index = categories.indexOf(oldCategory);
                if (index != -1) {
                    categories.set(index, trimmedName);
                    
                    // Update selected category if it was the renamed one
                    if (oldCategory.equals(selectedCategory)) {
                        selectedCategory = trimmedName;
                    }
                    
                    // Rename the tasks file
                    renameTasksFile(oldCategory, trimmedName);
                    
                    saveCategoriestoFile();
                    refreshCategoriesDisplay();
                }
            } else if (categories.contains(trimmedName)) {
                showAlert("Error", "Category name already exists!", Alert.AlertType.ERROR);
            }
        });
    }

    private void deleteCategory(String category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Category");
        alert.setHeaderText("Delete category: " + capitalizeFirst(category));
        alert.setContentText("This will permanently delete the category and all its tasks. Are you sure?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            categories.remove(category);
            
            // Clear selection if deleted category was selected
            if (category.equals(selectedCategory)) {
                selectedCategoryButton = null;
                selectedCategory = null;
            }
            
            // Delete associated tasks file
            deleteTasksFile(category);
            
            saveCategoriestoFile();
            refreshCategoriesDisplay();
            
            // Clear content pane if this category was currently displayed
            contentPane.getChildren().clear();
            contentPane.getChildren().add(new javafx.scene.control.Label("Select a category to view tasks"));
        }
    }

    private void renameTasksFile(String oldCategory, String newCategory) {
        if (Session.getUsername() == null) return;
        
        File oldFile = new File("tasks_" + Session.getUsername() + "_" + oldCategory + ".txt");
        File newFile = new File("tasks_" + Session.getUsername() + "_" + newCategory + ".txt");
        
        if (oldFile.exists()) {
            if (!oldFile.renameTo(newFile)) {
                System.err.println("Failed to rename tasks file from " + oldFile.getName() + " to " + newFile.getName());
            }
        }
    }

    private void deleteTasksFile(String category) {
        if (Session.getUsername() == null) return;
        
        File file = new File("tasks_" + Session.getUsername() + "_" + category + ".txt");
        if (file.exists()) {
            if (!file.delete()) {
                System.err.println("Failed to delete tasks file: " + file.getName());
            }
        }
    }

    private void saveCategoriestoFile() {
        if (Session.getUsername() == null) return;
        
        File file = new File("categories_" + Session.getUsername() + ".txt");
        try (PrintWriter writer = new PrintWriter(file)) {
            for (String category : categories) {
                writer.println(category);
            }
        } catch (IOException e) {
            System.err.println("Error saving categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCategoriesFromFile() {
        if (Session.getUsername() == null) return;
        
        File file = new File("categories_" + Session.getUsername() + ".txt");
        if (!file.exists()) return;

        categories.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String category = line.trim();
                if (!category.isEmpty()) {
                    categories.add(category);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading categories: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadContentWithCategory(String fxmlFile, String category) throws IOException {
        contentPane.getChildren().clear();

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent content = loader.load();

        // Set controller parameters
        todoController controller = loader.getController();
        controller.setUsername(Session.getUsername());
        controller.setCategory(category);

        contentPane.getChildren().add(content);
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}