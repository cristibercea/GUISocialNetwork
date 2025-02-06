package ubb.scs.map.guisocialnetwork.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import ubb.scs.map.guisocialnetwork.domain.entities.MessageDTO;
import ubb.scs.map.guisocialnetwork.domain.entities.Tuple;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.services.ServiceException;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ChatController implements Controller{
    private Service service;
    private Utilizator loggedUser;
    private Utilizator otherUser;
    private Stage stage;
    private final ObservableList<MessageDTO> model = FXCollections.observableArrayList();


    @FXML
    private TableView<MessageDTO> tableView;
    @FXML
    private TableColumn<MessageDTO, String> tableColumnOtherUser;
    @FXML
    private TableColumn<MessageDTO, String> tableColumnLoggedUser;
    @FXML
    private TextField textFieldSearchChat;
    @FXML
    private TextField textFieldNewMessage;

    @Override
    public void update(UtilizatorEntityChangeEvent event) {initModel();}

    @FXML
    public void initialize() {
        tableView.setFixedCellSize(-1);
        textFieldSearchChat.setPromptText("filter messages");
        textFieldNewMessage.setPromptText("share your thoughts...");
        tableColumnOtherUser.setCellValueFactory(new PropertyValueFactory<>("MessageLeft"));
        tableColumnLoggedUser.setCellValueFactory(new PropertyValueFactory<>("MessageRight"));
        tableView.setItems(model);
    }

    @Override
    public void setService(Service service, Stage stage, Utilizator u) {
        this.service = service;
        this.otherUser = u;
        this.stage = stage;
        service.addObserver(this);
        tableColumnOtherUser.setText(otherUser.getLastName()+ "'s Messages");
        initModel();
    }

    void initModel() {
        Iterable<MessageDTO> messages = service.getAllMessagesForFriendship(new Tuple<>(loggedUser, otherUser),tableView.getColumns().getFirst().getPrefWidth());
        List<MessageDTO> conversation = StreamSupport.stream(messages.spliterator(), false)
                .collect(Collectors.toList());
        model.setAll(conversation);
    }

    @Override
    public void setLoggedInUser(Utilizator u) { loggedUser = u; }

    public void handleTextFieldSearch(KeyEvent keyEvent) {
       if(textFieldSearchChat.getText().isEmpty()) {tableView.setItems(model); return;}
       Predicate<MessageDTO> filterSearch = u -> (u.getMessageLeft().toLowerCase()+u.getMessageRight().toLowerCase()).contains(textFieldSearchChat.getText().toLowerCase());
       tableView.setItems(model.filtered(filterSearch));
    }

    public void handleBack(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLoader.class.getResource("views/chat-selection-view.fxml"));

            AnchorPane userLayout = fxmlLoader.load();
            stage.setTitle("Open a chat");
            stage.setScene(new Scene(userLayout,320, 470));

            ChatSelectionController userController = fxmlLoader.getController();
            service.addObserver(userController);
            userController.setLoggedInUser(loggedUser);
            userController.setService(service, stage, loggedUser);
            service.removeObserver(this);
        } catch (IOException e) {e.printStackTrace();}
    }

    public void handleSend(ActionEvent keyEvent) {
        if(textFieldNewMessage.getText().isEmpty()) {return;}
        try {service.saveMessage(textFieldNewMessage.getText(), new Tuple<>(loggedUser,otherUser));}
        catch (ServiceException e){MessageAlert.showErrorMessage(null, e.getMessage());}
        finally {textFieldNewMessage.setText("");}
    }
}
