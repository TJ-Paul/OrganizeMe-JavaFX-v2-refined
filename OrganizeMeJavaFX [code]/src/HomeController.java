import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class HomeController{

    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private Button login;
    @FXML private Button signup;
    @FXML private Label wrongPass;
    @FXML private Label wronglogin;
    
    @FXML
    public void login_button(ActionEvent e) throws IOException{
        Login();
    }
    
    @FXML
    public void signup_button(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/signup.fxml");
    }



    public void Login() throws IOException {

        String user_name = username.getText().trim();
        String user_pass = password.getText().trim();

        if (user_name.isEmpty() || user_pass.isEmpty()) {
            wronglogin.setText("Please enter your data.");
            return;
        }

        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String storedUser = parts[0];
                String storedPass = parts[2];

                if (user_name.equals(storedUser) && user_pass.equals(storedPass)) {
                    found = true;
                    break;
                }
            }
        } catch (IOException e) {
            wronglogin.setText("Error reading user data.");
            e.printStackTrace();
            return;
        }

        if (found) {
            System.out.println("Success!");
            Session.setUsername(user_name);
            App.changeScene("fxmlFiles/menu.fxml");
        } else { wronglogin.setText("Wrong username / password!"); }
    }
}