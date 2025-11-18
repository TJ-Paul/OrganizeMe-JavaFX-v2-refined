import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class App extends Application {
    Stage window;

    
    public static void main(String[] args) {
        System.out.println("hello world!");
        launch(args);
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("demo");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("page1.fxml"));
        Parent root = loader.load();

        window.setScene(new Scene(root));
        window.getIcons().add(new Image("OrganizeMe_logo.png"));

        window.show();
    }

}