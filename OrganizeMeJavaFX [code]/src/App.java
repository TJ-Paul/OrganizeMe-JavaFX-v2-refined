import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class App extends Application{
    
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        launch(args);
    }



    private static Stage stage;
    @Override
    public void start(Stage primaryStage) throws Exception{
        stage = primaryStage;
        primaryStage.setResizable(false);
        FXMLLoader loader = new FXMLLoader(getClass().getResource("fxmlFiles/login.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("OrganizeMe");
        primaryStage.setScene(new Scene(root,600,400));
        primaryStage.show();
    }

    public Stage getStage(){
        return stage;
    }

    public static void changeScene(String fxml) throws IOException {
        Parent root = FXMLLoader.load(App.class.getResource(fxml));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
    }
    
}
