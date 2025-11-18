// exploring features of TEXT_INPUT_DIALOG pop-up
/*
 * Learned:
 *  - VBox
 *      - setspacing
 *      - setAlignment()
 *      - getChildren().add()
 *      - getChildren().remove()
 *  - ImageView
 *      - setFitWidth()
 *  - Button
 *      - setText
 *      - setOnAction
 *      # set action with lambda
 *      - fire()
 *  - addListener()
 *  - StringProperty
 *      #to function with addListener
 *  - TextInputDialog
 *      - setTitle()
 *      - setHeaderText()
 *      - setContentText()
 *      - getDialogPane().setGraphic()
 *      # set logo/icon for the pop-up window
 *  - TextField
 *      - setPromptText
 *      - setAlighnment(Pos.CENTER)
 *      - setOnAction()
 *      # fires on enter press
 *      - setPrefWidth
 *      - setMaxWidth
 */
 

import java.util.Optional;
import javafx.application.Application;
import javafx.beans.property.*;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class Text_input_dialog extends Application {
    Stage window;
    // String currentName = "user001";
    private final StringProperty currentName = new SimpleStringProperty("user001");

    
    @Override
    public void start(Stage primaryStage) throws Exception {
        VBox vBox = new VBox();
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.TOP_CENTER);


        window = primaryStage;
        window.setTitle("fx-tester");
        window.setScene(new Scene(vBox, 600, 400));
        window.show();
        
        Label label = new Label("Welcome to the site ");
        vBox.getChildren().add(label);
        
        Button btn = new Button("name");
        vBox.getChildren().add(btn);
        
        TextField age = new TextField();
        age.setPromptText("enter age");
        age.setAlignment(Pos.CENTER);
        // age.setPrefWidth(50);
        age.setMaxWidth(100);
        age.setOnAction(e -> {
            age.setOnAction(a -> btn.fire());
        });

        vBox.getChildren().add(age);
        
        ImageView qMark = new ImageView(new Image("ques_mark.png"));
        qMark.setFitWidth(60);
        qMark.setFitHeight(60);
        vBox.getChildren().add(qMark);
        


        currentName.addListener((obs, oldName, newName)->{
            if (!newName.equals("user001")){ 
                btn.setText("rename");
            } 
            else { btn.setText("name/ rename"); }
        });


        
        btn.setOnAction(e ->{
            System.out.println("pressed!");

            ImageView icon = new ImageView(new Image("netowrk_signal.png"));
            icon.setFitWidth(50);
            icon.setFitHeight(50);
            
            TextInputDialog serverDialog = new TextInputDialog(currentName.get());
            serverDialog.setTitle("Connect to Project Server");
            serverDialog.setHeaderText("Server Connection");
            serverDialog.setContentText("Enter server address:");
            serverDialog.getDialogPane().setGraphic(icon);

            
            Optional<String> result = serverDialog.showAndWait();
            result.ifPresent(name ->{
                currentName.set(name);
                label.setText("Welcome to the site " + name);
            });
        });
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}