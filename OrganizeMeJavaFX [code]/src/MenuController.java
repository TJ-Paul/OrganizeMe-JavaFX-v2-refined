import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class MenuController{
    @FXML private Button taskManagementBtn;
    @FXML private Button pomodoroBtn;
    @FXML private Button trackStreakBtn;
    @FXML private Button logoutBtn;
    @FXML private Button workTogetherBtn;

    @FXML 
    public void loadTaskManagement(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/sidebar.fxml");
    }

    @FXML
    public void loadPomodoro(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/pomodoro.fxml");
    }

    @FXML
    public void loadTrackStreak(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/streak.fxml");
    }

    @FXML
    public void workTogether(ActionEvent event) throws Exception {
        workTogether();
    }

    @FXML
    public void userLogout(ActionEvent event) throws IOException {
        Session.clear();
        App.changeScene("fxmlFiles/login.fxml");
    }



    public void workTogether() throws Exception{
            Stage projectStage = new Stage();
            projectStage.setResizable(false);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/project.fxml"));
            Parent root = loader.load();
            
            projectStage.setTitle("Project Manager");
            projectStage.setScene(new Scene(root,800,600));
            projectStage.show();
            
            ProjectController controller = loader.getController();
            controller.setPrimaryStage(projectStage);
            
            // Handle application close
            projectStage.setOnCloseRequest(e -> {
                controller.disconnect();
                projectStage.close();
            });

            // Show connection dialog
            controller.showConnectionDialog();
    }
}