import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class SignupController{

    
    @FXML private TextField username;
    @FXML private TextField email;
    @FXML private PasswordField password;
    @FXML private Button login;
    @FXML private Button signup;
    @FXML private Label signup_error;

    @FXML 
    void login_button(ActionEvent event) throws IOException {
        App.changeScene("fxmlFiles/login.fxml");
    }
    
    @FXML
    void signup_button(ActionEvent event) throws IOException {
        SignUp();
    }



    private void SignUp() throws IOException{
        String user = username.getText().trim();
        String mail = email.getText().trim();
        String pass = password.getText().trim();
        
        
        if (userExists(user)) {
            signup_error.setText("Username already exists.");
            return;
        }

        if (emailExists(mail)) {
            signup_error.setText("Email already used.");
            return;
        }

        if (user.isEmpty() || pass.isEmpty() || mail.isEmpty()) {
            if (user.isEmpty()){signup_error.setText("Username is required.");} 
            else if (mail.isEmpty()){signup_error.setText("Email is required.");} 
            else if (pass.isEmpty()){signup_error.setText("Password is required.");}
        } 
        
        else if (!mail.endsWith("@gmail.com")) {
            signup_error.setText("not a proper mail address");
        } 
        
        else {
            try (FileWriter fw = new FileWriter("users.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw)) {
                out.println(user + "," + mail + "," + pass);
            } catch (IOException e) {
                signup_error.setText("Failed to save user data.");
                e.printStackTrace();
                return;
            }
            
            Session.setUsername(user); 
            signup_error.setText(""); 
            App.changeScene("fxmlFiles/menu.fxml");
        }
    }


    private boolean userExists(String username) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String storedUser = parts[0];
                if (storedUser.equalsIgnoreCase(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean emailExists(String email) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String storedEmail = parts[1];
                if (storedEmail.equalsIgnoreCase(email)) {
                    return true;
                }
            }
        }
        return false;
    }
}
