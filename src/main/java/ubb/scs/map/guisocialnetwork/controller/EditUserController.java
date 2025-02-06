package ubb.scs.map.guisocialnetwork.controller;

import org.mindrot.jbcrypt.BCrypt;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.domain.validators.ValidationException;
import ubb.scs.map.guisocialnetwork.services.Service;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import ubb.scs.map.guisocialnetwork.services.ServiceException;
import ubb.scs.map.guisocialnetwork.utils.ProblemReporter;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;

public class EditUserController implements Controller{
    @FXML
    private TextField textFieldNewPassword;
    @FXML
    private TextField textFieldConfirmPassword;
    @FXML
    private TextField textFieldId;
    @FXML
    private Label labelId;
    @FXML
    private TextField textFieldFirstName;
    @FXML
    private TextField textFieldLastName;
    @FXML
    private TextField textFieldEmail;


    private Service service;
    private Stage dialogStage;

    @FXML
    private void initialize() {}

    @Override
    public void setLoggedInUser(Utilizator u) {}

    @Override
    public void setService(Service service,  Stage stage, Utilizator u) {
        textFieldFirstName.setPromptText("your first name");
        textFieldLastName.setPromptText("your last name");
        textFieldEmail.setPromptText("insert your email");
        textFieldNewPassword.setPromptText("insert new password");
        textFieldConfirmPassword.setPromptText("confirm new password");
        this.service = service;
        this.dialogStage=stage;
        setFields(u);
        textFieldId.setEditable(false);
    }

    @FXML
    public void handleUpdate(){
        String firstNameText= textFieldFirstName.getText();
        String lastNameText= textFieldLastName.getText();
        String emailText= textFieldEmail.getText();
        String errors="";
        if (firstNameText.isEmpty()) errors+="First name cannot be empty. ";
        if (lastNameText.isEmpty()) errors+="Last name cannot be empty. ";
        if (emailText.isEmpty()) errors+="Email cannot be empty.";
        if(!errors.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Something went wrong while processing your data.");
            alert.setContentText(errors);
            alert.showAndWait();
            return;
        }
        Utilizator utilizator1=new Utilizator(firstNameText,lastNameText,emailText);
        utilizator1.setId(Long.valueOf(textFieldId.getText()));
        updateAccount(utilizator1);
    }

    private void updateAccount(Utilizator m)
    {
        try {
            if(!textFieldNewPassword.getText().isEmpty()) {
                if (textFieldNewPassword.getText().equals(textFieldConfirmPassword.getText())) {
                    this.service.updateUtilizator(m);
                    service.setPassword(BCrypt.hashpw(textFieldNewPassword.getText(), BCrypt.gensalt()), m.getId());
                    MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Account Update", "Your account and password have been updated successfully!");
                    dialogStage.close();
                } else MessageAlert.showErrorMessage(null, "Password confirmation failed!");
            } else {
                this.service.updateUtilizator(m);
                MessageAlert.showMessage(null, Alert.AlertType.INFORMATION, "Account Update", "Your account has been updated successfully!");
                dialogStage.close();
            }
        } catch (ValidationException | ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    private void setFields(Utilizator u)
    {
        textFieldId.setText(u.getId().toString());
        textFieldFirstName.setText(u.getFirstName());
        textFieldLastName.setText(u.getLastName());
        textFieldEmail.setText(u.getEmail());
    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }

    @Override
    public void update(UtilizatorEntityChangeEvent utilizatorEntityChangeEvent) {}
}
