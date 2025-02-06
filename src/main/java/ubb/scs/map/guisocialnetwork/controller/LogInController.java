package ubb.scs.map.guisocialnetwork.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ubb.scs.map.guisocialnetwork.ApplicationLoader;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.domain.validators.EmailValidator;
import ubb.scs.map.guisocialnetwork.domain.validators.ValidationException;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.services.ServiceException;
import ubb.scs.map.guisocialnetwork.utils.ProblemReporter;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

public class LogInController implements Controller {
    @FXML
    private Button buttonSignUp;
    @FXML
    private Button buttonLogin;
    @FXML
    private Label labelWelcome;
    @FXML
    private Label labelFirstName;
    @FXML
    private Label labelLastName;
    @FXML
    private Label labelConfirmation;
    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldLastName;
    @FXML
    private TextField textFieldEmail;
    @FXML
    private PasswordField textFieldPassword;
    @FXML
    private PasswordField textFieldConfirmPassword;


    private Service service;
    private Utilizator loggedUser;
    Stage dialogStage;

    @FXML
    private void initialize() {}

    @Override
    public void setLoggedInUser(Utilizator u) {
        loggedUser = u;
    }

    @Override
    public void setService(Service service,  Stage stage, Utilizator utilizator) {
        textFieldFirstName.setPromptText("your first name");
        textFieldLastName.setPromptText("your last name");
        textFieldEmail.setPromptText("insert your email");
        textFieldPassword.setPromptText("insert your password");
        textFieldConfirmPassword.setPromptText("confirm your password");
        this.service = service;
        this.dialogStage=stage;
        loginSetup();
    }

    @FXML
    public void handleLogin(){
        if(buttonLogin.getText().equals("Sign Me In!")) {loginSetup(); return;}
        String emailText= textFieldEmail.getText();
        String passwordText= textFieldPassword.getText();

        try {
            if (logInUser(emailText, passwordText)) {
                try {
                    FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLoader.class.getResource("views/utilizator-view.fxml"));

                    AnchorPane userLayout = fxmlLoader.load();
                    dialogStage.setTitle("Facebook Wannabe");
                    dialogStage.setScene(new Scene(userLayout));
                    dialogStage.setWidth(800);

                    UtilizatorController userController = fxmlLoader.getController();
                    userController.setService(service, dialogStage, loggedUser);
                } catch (IOException e) {e.printStackTrace();}
            }
        } catch (ServiceException e){ProblemReporter.report(e.getMessage());}
    }

    @FXML
    public void handleSignUp(){
        if(buttonSignUp.getText().equals("Sign Me Up!")) {
            textFieldPassword.setPromptText("create your password");
            dialogStage.setTitle("Sign Up Menu");
            buttonSignUp.setText("Create Account");
            buttonLogin.setText("Sign Me In!");
            labelWelcome.setText("Sign Up by inserting following data:");
            labelFirstName.setVisible(true);
            labelLastName.setVisible(true);
            labelConfirmation.setVisible(true);
            textFieldFirstName.setVisible(true);
            textFieldLastName.setVisible(true);
            textFieldConfirmPassword.setVisible(true);
            return;
        }
        try {
            if (signUpUser()) {
                clearFields();
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Sign Up");
                alert.setHeaderText("Sign Up Successful!");
                alert.setContentText("Account created successfully!");
                alert.showAndWait();
                loginSetup();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Sign Up");
                alert.setHeaderText("Sign Up Failed!");
                alert.setContentText("Something went wrong in account creation. Please check your personal data and try again!");
                alert.showAndWait();
            }
        } catch (ServiceException e){ProblemReporter.report(e.getMessage());}
    }

    private boolean logInUser(String emailText, String passwordText) throws ServiceException {
        if(emailText.isEmpty() || passwordText.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed!");
            alert.setContentText("Please enter your email address and password!");
            alert.showAndWait();
            return false;
        }
        if(notAnEmail(emailText)) return false;
        if(service.getPassword(emailText)==null){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed!");
            alert.setContentText("There is no account with provided email address. You could try to sign up!");
            alert.showAndWait();
            return false;
        }
        if(BCrypt.checkpw(passwordText, service.getPassword(emailText))) {
            loggedUser = service.getUserByEmail(emailText);
            return true;
        }
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Login Failed!");
        alert.setContentText("Password provided for "+emailText+" is incorrect!");
        alert.showAndWait();
        return false;
    }

    private boolean signUpUser() throws ServiceException {
        String firstNameText= textFieldFirstName.getText();
        String lastNameText= textFieldLastName.getText();
        String emailText= textFieldEmail.getText();
        String passwordText= textFieldPassword.getText();
        String confirmPasswordText= textFieldConfirmPassword.getText();

        if(emailText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty() || lastNameText.isEmpty() || firstNameText.isEmpty())
            return false;
        if(notAnEmail(emailText)) return false;
        if(!passwordText.equals(confirmPasswordText)) return false;
        Utilizator user = new Utilizator(firstNameText,lastNameText,emailText);
        service.addUtilizator(user);
        service.setPassword(BCrypt.hashpw(passwordText, BCrypt.gensalt()), service.getUserByEmail(emailText).getId());
        return true;
    }

    private void clearFields() {
        textFieldFirstName.setText("");
        textFieldLastName.setText("");
        textFieldEmail.setText("");
        textFieldPassword.setText("");
        textFieldConfirmPassword.setText("");
    }

    private void loginSetup(){
        clearFields();
        textFieldPassword.setPromptText("insert your password");
        dialogStage.setTitle("Sign In Menu");
        buttonSignUp.setText("Sign Me Up!");
        buttonLogin.setText("Log In");
        labelWelcome.setText("Sign In by inserting following data:");
        labelFirstName.setVisible(false);
        labelLastName.setVisible(false);
        labelConfirmation.setVisible(false);
        textFieldFirstName.setVisible(false);
        textFieldLastName.setVisible(false);
        textFieldConfirmPassword.setVisible(false);
    }

    private boolean notAnEmail(String emailText) {
        try {
            EmailValidator validator = new EmailValidator();
            validator.validate(emailText);
        } catch (ValidationException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Login Failed!");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
            return true;
        }
        return false;
    }

    @Override
    public void update(UtilizatorEntityChangeEvent utilizatorEntityChangeEvent) {}
}
