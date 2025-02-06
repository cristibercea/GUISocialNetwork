package ubb.scs.map.guisocialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import ubb.scs.map.guisocialnetwork.domain.entities.Friendship;
import ubb.scs.map.guisocialnetwork.domain.entities.Tuple;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.services.ServiceException;
import ubb.scs.map.guisocialnetwork.utils.ProblemReporter;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import ubb.scs.map.guisocialnetwork.utils.observer.Observer;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AddFriendController implements Controller{
    private Service service;
    private final ObservableList<Utilizator> model = FXCollections.observableArrayList();
    private Stage stage;
    private Utilizator loggedUser;

    @FXML
    private Button buttonNewFriend;
    @FXML
    private Button buttonReject;
    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator, String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnLastName;
    @FXML
    private TextField textFieldName;

    @Override
    public void setService(Service service, Stage stage, Utilizator u) {
        this.service = service;
        this.stage = stage;
        if(u==null) {
            buttonNewFriend.setText("Add Friend");
            buttonNewFriend.setOnAction(_ -> handleAddFriend(null));
            buttonReject.setVisible(false);
            textFieldName.prefWidth(600);
            buttonReject.setMaxWidth(0);
            initModelCreateFriendRequest();
        }
        else{
            buttonNewFriend.setText("Accept");
            buttonNewFriend.setOnAction(_ -> handleAcceptFriendRequest(null));
            buttonReject.setVisible(true);
            initModelManageFriendRequests();
        }
    }



    @Override
    public void setLoggedInUser(Utilizator u) {this.loggedUser = u;}

    @FXML
    public void initialize() {
        textFieldName.setPromptText("search for a person by name");
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableView.setItems(model);
    }

    private void initModelCreateFriendRequest() {
        Iterable<Utilizator> messages = service.getAllNonFriends(loggedUser);
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(users);
    }

    private void initModelManageFriendRequests() {
        Iterable<Utilizator> messages = service.getAllReceivedForCurrentUser(loggedUser);
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(users);
    }

    public void handleAddFriend(ActionEvent actionEvent) {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            MessageAlert.showErrorMessage(null,"You must select a person from the list of people!");
            return;
        }
        Utilizator u = tableView.getSelectionModel().getSelectedItem();
        Friendship friendship = new Friendship();
        friendship.setId(new Tuple<>(loggedUser, u));
        friendship.setStatus("pending");
        try {
            service.addFriendship(friendship, loggedUser);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Friendship requested", u.getFirstName() + " " + u.getLastName() + " received your friend request!");
            stage.close();
        } catch (ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    public void handleAcceptFriendRequest(ActionEvent actionEvent) {
        if (friendRequestProcessingFailed()) return;
        Utilizator u = tableView.getSelectionModel().getSelectedItem();
        Friendship friendship = new Friendship();
        friendship.setId(new Tuple<>(u, loggedUser));
        friendship.setStatus("accepted");
        try {
            service.updateFriendship(friendship);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Friendship accepted :)", u.getFirstName() + " " + u.getLastName() + " is now your friend!");
            stage.close();
        } catch (ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    public void handleRejectFriendRequest(ActionEvent actionEvent) {
        if (friendRequestProcessingFailed()) return;
        Utilizator u = tableView.getSelectionModel().getSelectedItem();
        try {
            Friendship friendship = new Friendship();
            friendship.setId(new Tuple<>(u, loggedUser));
            friendship.setStatus("rejected");
            service.removeFriendship(friendship, loggedUser);
            MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Friendship rejected :(", u.getFirstName() + " " + u.getLastName() + " will not be notified!");
            stage.close();
        } catch (ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    public void handleSelectionChanged(MouseEvent mouseEvent) {
        textFieldName.setText(tableView.getSelectionModel().getSelectedItem().getFirstName()+" "+tableView.getSelectionModel().getSelectedItem().getLastName());
    }

    public void handleTextFieldSearch(KeyEvent keyEvent) {
        if(textFieldName.getText().isEmpty()) {tableView.setItems(model); return;}
        Predicate<Utilizator> filterSearch = u -> (u.getFirstName().toLowerCase()+" "+u.getLastName().toLowerCase()).contains(textFieldName.getText().toLowerCase());
        tableView.setItems(model.filtered(filterSearch));
    }

    private boolean friendRequestProcessingFailed() {
        if (tableView.getSelectionModel().getSelectedItem() == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Friend Request Error");
            alert.setHeaderText("Friend Request Processing Error");
            alert.setContentText("You must select a person's friend request from the list!");
            alert.showAndWait();
            return true;
        }
        return false;
    }

    @Override
    public void update(UtilizatorEntityChangeEvent utilizatorEntityChangeEvent) {}
}
