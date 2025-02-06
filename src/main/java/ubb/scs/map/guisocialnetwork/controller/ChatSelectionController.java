package ubb.scs.map.guisocialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import ubb.scs.map.guisocialnetwork.ApplicationLoader;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import ubb.scs.map.guisocialnetwork.utils.observer.Observer;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ChatSelectionController implements Observer<UtilizatorEntityChangeEvent>, Controller {
    private Service service;
    private Utilizator loggedUser;
    private Stage stage;
    private final ObservableList<Utilizator> model = FXCollections.observableArrayList();

    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator, String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator, String> tableColumnLastName;
    @FXML
    private TextField textFieldSearchChat;

    @Override
    public void update(UtilizatorEntityChangeEvent event) {initModel();}

    @FXML
    public void initialize() {
        textFieldSearchChat.setPromptText("search for a chat");
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableView.setFixedCellSize(50);
        tableView.setItems(model);
    }

    @Override
    public void setService(Service service, Stage stage, Utilizator u) {
        this.service = service;
        this.loggedUser = u;
        this.stage = stage;
        initModel();
    }

    void initModel() {
        Iterable<Utilizator> messages = service.getAllForCurrentUser(loggedUser);
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(users);
    }

    @Override
    public void setLoggedInUser(Utilizator u) {}

    public void handleTextFieldSearch(KeyEvent keyEvent) {
        if(textFieldSearchChat.getText().isEmpty()) {tableView.setItems(model); return;}
        Predicate<Utilizator> filterSearch = u -> (u.getFirstName().toLowerCase()+" "+u.getLastName().toLowerCase()).contains(textFieldSearchChat.getText().toLowerCase());
        tableView.setItems(model.filtered(filterSearch));
    }

    public void handleSelection(){
        try {
            Utilizator friend = tableView.getSelectionModel().getSelectedItem();
            if(friend == null) {return;}
            FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLoader.class.getResource("views/chat-view.fxml"));

            AnchorPane userLayout = fxmlLoader.load();
            stage.setTitle("Chat: " + friend.getFirstName() + " " + friend.getLastName());
            stage.setScene(new Scene(userLayout,340, 500));

            ChatController userController = fxmlLoader.getController();
            service.addObserver(userController);
            userController.setLoggedInUser(loggedUser);
            userController.setService(service, stage, friend);
            service.removeObserver(this);
        } catch (IOException e) {e.printStackTrace();}
    }
}
