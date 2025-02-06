package ubb.scs.map.guisocialnetwork.controller;

import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import ubb.scs.map.guisocialnetwork.ApplicationLoader;
import ubb.scs.map.guisocialnetwork.domain.entities.*;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.services.ServiceException;
import ubb.scs.map.guisocialnetwork.utils.ProblemReporter;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import ubb.scs.map.guisocialnetwork.utils.observer.Observer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class UtilizatorController implements Observer<UtilizatorEntityChangeEvent>, Controller {
    private Service service;
    private final ObservableList<Utilizator> model = FXCollections.observableArrayList();
    private Stage stage;
    private Utilizator loggedUser;
    private int friendRequests=-1;
    private Pageable pageable;
    private long numberOfPages =0;

    @FXML
    private SplitPane splitPane;
    @FXML
    private ImageView imageView;
    @FXML
    private Label labelPageNumber;
    @FXML
    private Button buttonPrevPage;
    @FXML
    private Button buttonNextPage;
    @FXML
    private ComboBox<String> comboBox;
    @FXML
    private Label labelLogedUser;
    @FXML
    private TableView<Utilizator> tableView;
    @FXML
    private TableColumn<Utilizator,String> tableColumnFirstName;
    @FXML
    private TableColumn<Utilizator,String> tableColumnLastName;
    @FXML
    private TableColumn<Utilizator,String> tableColumnEmail;

    @Override
    public void setService(Service service,Stage stage,Utilizator utilizator) {
        pageable = new Pageable(1,-1);
        this.stage = stage;
        this.service = service;
        this.loggedUser = utilizator;
        this.imageView.setClip(new Circle(55,55, 55));
        this.imageView.setVisible(true);
        labelLogedUser.setText("Welcome, "+loggedUser.getFirstName()+" "+loggedUser.getLastName()+"! Here is your friend list:");
        service.addObserver(this);
        initComboBox();
        initModel();
    }

    @Override
    public void setLoggedInUser(Utilizator u) {}

    @FXML
    public void initialize() {
        tableColumnFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        tableColumnLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        tableColumnEmail.setCellValueFactory(new PropertyValueFactory<>("Email"));
        tableView.setItems(model);
    }

    private void initComboBox() {
        ObservableList<String> options = FXCollections.observableArrayList();
        options.add("All");
        for(int nr=1; nr < service.getNumberOfFriends(loggedUser); nr++) options.add(String.valueOf(nr));
        comboBox.setItems(options);
        comboBox.getSelectionModel().selectFirst();
    }

    private void initModel() {
        Iterable<Utilizator> messages = service.getAllForCurrentUser(loggedUser);
        List<Utilizator> users = StreamSupport.stream(messages.spliterator(), false).collect(Collectors.toList());
        model.setAll(users);
        newFriendRequestNotification();
    }

    private void newFriendRequestNotification() {
        int currentFriendRequests =
                StreamSupport.stream(service.getAllReceivedForCurrentUser(loggedUser).spliterator(), false).toList().size();
        if(currentFriendRequests==0) {friendRequests=0; return;}
        if(friendRequests==-1) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Requests");
            alert.setHeaderText(null);
            alert.setContentText("You have "+currentFriendRequests+" unaddressed friend requests.");
            alert.showAndWait();
            friendRequests=currentFriendRequests;
            return;
        }
        if(currentFriendRequests>friendRequests) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Friend Requests");
            alert.setHeaderText(null);
            alert.setContentText("You have received a new friend request!");
            alert.showAndWait();
        }
        friendRequests=currentFriendRequests;
    }

    @Override
    public void update(UtilizatorEntityChangeEvent utilizatorEntityChangeEvent) {initModel();initComboBox();}

    public void handleDeleteAccount(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.WARNING, "", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Account deletion");
        alert.setHeaderText("Delete your account?");
        alert.setContentText("You are about to delete this account. This action is irreversible and your data will be lost. Are you sure you want to proceed?");
        alert.showAndWait();
        if(alert.getResult() == ButtonType.YES)
            try {
                service.deleteUtilizator(loggedUser.getId());
                handleLogOut(actionEvent);
            } catch (ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    public void handleUpdateAccount(ActionEvent actionEvent) {
        Controller editUtilizatorController = new EditUserController();
        showMessageTaskDialog(loggedUser, editUtilizatorController,"/ubb/scs/map/guisocialnetwork/views/edit-user-view.fxml", "Edit Profile");
        loggedUser = service.getUtilizator(loggedUser.getId()).orElse(null);
        labelLogedUser.setText("Welcome, "+loggedUser.getFirstName()+" "+loggedUser.getLastName()+"! Here is your friend list:");
    }

    public void handleLogOut(ActionEvent actionEvent) {
        service.removeObserver(this);
        loggedUser = null;
        try {ApplicationLoader.initLogin(stage);}
        catch (IOException e) {e.printStackTrace();}
    }

    public void handleAddFriend(ActionEvent actionEvent) {
        Controller addFriendController = new AddFriendController();
        showMessageTaskDialog(null, addFriendController,"/ubb/scs/map/guisocialnetwork/views/add-friend-view.fxml", "Add friend");
    }

    public void handleDeleteFriend(ActionEvent actionEvent) {
        Utilizator user= tableView.getSelectionModel().getSelectedItem();
        try {
            if (user != null) {
                Friendship deleted = new Friendship();
                deleted.setId(new Tuple<>(loggedUser, user));
                deleted.setStatus("accepted");
                deleted = service.removeFriendship(deleted,loggedUser);
                MessageAlert.showMessage(null, Alert.AlertType.CONFIRMATION, "Delete friendship", "You and " + user.getFirstName() + " " + user.getLastName() + " are not friends anymore!");
            } else MessageAlert.showErrorMessage(null, "No friend has been selected in the list!");
        } catch (ServiceException e) {ProblemReporter.report(e.getMessage());}
    }

    public void handleFriendRequests(ActionEvent actionEvent) {
        Controller addFriendController = new AddFriendController();
        showMessageTaskDialog(loggedUser, addFriendController, "/ubb/scs/map/guisocialnetwork/views/add-friend-view.fxml", "Friend requests");
    }

    public void handleOpenChat(ActionEvent actionEvent) {
        Controller chatController = new ChatSelectionController();
        showMessageTaskDialog(loggedUser, chatController, "/ubb/scs/map/guisocialnetwork/views/chat-selection-view.fxml", "Open a chat");
    }

    private void showMessageTaskDialog(Utilizator user, Controller controller, String path, String title) {
        try {
            // create a new stage for the popup dialog.
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource(path));
            AnchorPane root = (AnchorPane) loader.load();

            // Create the dialog Stage.
            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            Scene scene = new Scene(root);
            dialogStage.setScene(scene);

            controller = loader.getController();
            if(title.equals("Friend requests")||title.equals("Add friend")) controller.setLoggedInUser(loggedUser);
            controller.setService(service, dialogStage, user);
            service.addObserver(controller);
            dialogStage.showAndWait();
            service.removeObserver(controller);
        } catch ( IOException e) {e.printStackTrace();}
    }

    private void initPagedModel() {
        Page<Utilizator> friends = service.getAllForCurrentUserPaged(loggedUser,pageable);
        model.setAll(StreamSupport.stream(friends.getObjects().spliterator(),false).collect(Collectors.toList()));
    }

    public void handlePreviousPage(ActionEvent actionEvent) {
        pageable.setPage(pageable.getPage()-1);
        if(pageable.getPage()==-1) {pageable.setPage(0); return;}
        labelPageNumber.setText("Page "+ (pageable.getPage()+1) +" of "+numberOfPages);
        initPagedModel();
    }

    public void handleNextPage(ActionEvent actionEvent) {
        pageable.setPage(pageable.getPage()+1);
        if(pageable.getPage()==numberOfPages) {pageable.setPage((int) numberOfPages-1); return;}
        labelPageNumber.setText("Page "+ (pageable.getPage()+1) +" of "+numberOfPages);
        initPagedModel();
    }

    public void handlePageSizeChanged(ActionEvent actionEvent) {
        String selectedItem = comboBox.getSelectionModel().getSelectedItem();
        if(selectedItem==null) return;
        if(selectedItem.equals("All")) {
            splitPane.setDividerPositions(0.8);
            buttonNextPage.setVisible(false);
            buttonPrevPage.setVisible(false);
            labelPageNumber.setVisible(false);
            pageable.setPage(0);
            pageable.setSize(-1);
            initModel();
            return;
        }
        pageable.setPage(0);
        pageable.setSize(Integer.parseInt(selectedItem));
        numberOfPages = service.getNumberOfFriends(loggedUser)/pageable.getSize();
        if(pageable.getSize()>1) numberOfPages++;
        if(pageable.getSize()>1 && service.getNumberOfFriends(loggedUser)%pageable.getSize()==0) numberOfPages--;
        labelPageNumber.setText("Page 1 of " + numberOfPages);
        labelPageNumber.setVisible(true);
        buttonNextPage.setVisible(true);
        buttonPrevPage.setVisible(true);
        splitPane.setDividerPositions(0.7);
        initPagedModel();
    }
}
