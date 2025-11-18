import java.io.IOException;
import java.util.Optional;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.util.Duration;

public class PomodoroController{

    @FXML private Button startBtn;
    @FXML private Button pauseBtn;
    @FXML private Button resetBtn;

    @FXML private Label timerLabel;
    @FXML private Label sessionLabel;
    @FXML private Label sessionsLabel;

    @FXML private Button workBtn;
    @FXML private Button shortBreakBtn;
    @FXML private Button longBreakBtn;
    @FXML private Button finishBtn;
    
    @FXML private Button viewStreakBtn;
    @FXML private Button viewTasksBtn;
    @FXML private Button backToMenuBtn;

    private Timeline timeline;
    private int currentSeconds;
    private int completedSessions = 0;
    private int focus_duration = 0;
    private boolean isRunning = false;
    private PomodoroMode currentMode = PomodoroMode.WORK;

    // Timer durations 
    private static final int WORK_DURATION = 25 * 60; // 30 minutes
    private static final int SHORT_BREAK_DURATION = 5 * 60; // 5 minutes
    private static final int LONG_BREAK_DURATION = 15 * 60; // 15 minutes

    private enum PomodoroMode {
        WORK, SHORT_BREAK, LONG_BREAK
    }

    public void initialize() {
        setWorkMode();
        updateButtons();
    }

    @FXML
    public void startTimer(ActionEvent event) {
        if (!isRunning) {
            isRunning = true;
            startCountdown();
            updateButtons();
        }
    }

    @FXML
    public void pauseTimer(ActionEvent event) {
        if (isRunning) {
            isRunning = false;
            if (timeline != null) { timeline.stop(); }
            updateButtons();
        }
    }

    @FXML
    public void resetTimer(ActionEvent event) {
        isRunning = false;
        if (timeline != null) { timeline.stop(); }
        
        switch (currentMode) {
            case WORK: currentSeconds = WORK_DURATION; break;
            case SHORT_BREAK: currentSeconds = SHORT_BREAK_DURATION; break;
            case LONG_BREAK: currentSeconds = LONG_BREAK_DURATION; break;
        }
        
        updateDisplay();
        updateButtons();
    }

    @FXML
    public void setWorkMode(ActionEvent event) {
        setWorkMode();
    }

    @FXML
    public void setShortBreakMode(ActionEvent event) {
        setShortBreakMode();
    }

    @FXML
    public void setLongBreakMode(ActionEvent event) {
        setLongBreakMode();
    }



    @FXML
    public void viewStreak(ActionEvent event) throws IOException {
        alertAndNavigate(
            "Back to Streak!!", 
            "fxmlFiles/streak.fxml");
    }

    @FXML
    public void viewTasks(ActionEvent event) throws IOException {
        alertAndNavigate(
            "Back to Task Management!!", 
            "fxmlFiles/sidebar.fxml");
    }

    @FXML
    public void backToMenu(ActionEvent event) throws IOException {
        alertAndNavigate(
            "Back to Menu!!", 
            "fxmlFiles/menu.fxml");
    }

    @FXML
    public void plus_five(ActionEvent event){
        int maxSeconds = 180 * 60; // 3 hours max
        if (currentSeconds + (5 * 60) <= maxSeconds) {
            currentSeconds += 5 * 60;
            updateDisplay();
        } else {
            showAlert("Maximum Time", "Cannot exceed 180 minutes!");
        }
    }
    
    @FXML
    public void minus_five(ActionEvent event){
        if (currentSeconds - (5 * 60) >= 0) {
            currentSeconds -= 5 * 60;
            updateDisplay();
        } else {
            showAlert("Minimum Time", "Cannot go below 0 minutes!");
        }
    }

    @FXML
    public void session_done(ActionEvent event){
        timerFinished();
        System.out.println("session count: " + completedSessions);
    }




    private void setWorkMode() {
        currentMode = PomodoroMode.WORK;
        currentSeconds = WORK_DURATION;
        sessionLabel.setText("Work Session");
        updateDisplay();
        resetTimerState();
    }

    private void setShortBreakMode() {
        currentMode = PomodoroMode.SHORT_BREAK;
        currentSeconds = SHORT_BREAK_DURATION;
        sessionLabel.setText("Short Break");
        updateDisplay();
        resetTimerState();
    }

    private void setLongBreakMode() {
        currentMode = PomodoroMode.LONG_BREAK;
        currentSeconds = LONG_BREAK_DURATION;
        sessionLabel.setText("Long Break");
        updateDisplay();
        resetTimerState();
    }

    private void resetTimerState() {
        isRunning = false;
        if (timeline != null) {
            timeline.stop();
        }
        updateButtons();
    }

    private void startCountdown() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            currentSeconds--;
            if(currentMode == PomodoroMode.WORK){ focus_duration++; }
            updateDisplay();
            
            if(currentSeconds<=0){ timerFinished(); }
        }));

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void timerFinished() {
        isRunning = false;
        completedSessions++;
        timeline.stop();
        
        if (currentMode == PomodoroMode.WORK) {
            int hr = focus_duration/3600;
            int min = focus_duration/60;
            int sec = focus_duration%60;
            sessionsLabel.setText("focus duration: " + hr + " h : " + min + " m : " + sec + " s");
            
            // Auto-switch to appropriate break
            if (completedSessions % 4 == 0) {
                setLongBreakMode();
                showAlert("Work session complete!", "Time for a long break! (15 minutes)");
            } else {
                setShortBreakMode();
                showAlert("Work session complete!", "Time for a short break! (5 minutes)");
            }
        } else {
            setWorkMode();
            showAlert("Break finished!", "Ready for another work session? (25 minutes)");
        }
        
        updateButtons();
    }

    private void updateDisplay() {
        int minutes = currentSeconds / 60;
        int seconds = currentSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    private void updateButtons() {
        startBtn.setDisable(isRunning);
        pauseBtn.setDisable(!isRunning);
        workBtn.setDisable(isRunning);
        shortBreakBtn.setDisable(isRunning);
        longBreakBtn.setDisable(isRunning);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void alertAndNavigate(String title, String fxml) throws IOException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Want to break focus?");
        alert.setContentText("This will reset the timer!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (timeline != null) { timeline.stop(); }
            App.changeScene(fxml);
        }
    }
}