import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.beans.binding.Bindings;

public class page1_controller {

    @FXML private Button add_task_button;
    @FXML private TextField task_string;
    @FXML private ScrollPane scrollPane;   
    @FXML private VBox listBox;            


    @FXML
    private void initialize() {
        add_task_button.disableProperty().bind(task_string.textProperty().isEmpty());

        // press 'Enter' to add task
        task_string.setOnAction(e -> add_task(null)); 
    }


    @FXML
    void add_task(ActionEvent event) {

        String text = task_string.getText().trim();
        if (text.isEmpty()) return;

        Label lbl = new Label(text);
        lbl.setWrapText(true);
        lbl.setMaxWidth(Double.MAX_VALUE);

        lbl.setStyle(
            "-fx-padding: 8;" +
            "-fx-background-color: #dbdbdba2;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #737373ff;" +
            "-fx-border-radius: 3;" +
            "-fx-font-weight: bold;"
        );

        listBox.getChildren().add(lbl);
        task_string.clear();

        // Snap to bottom
        // scrollPane.setVvalue(1.0);
    }
}
