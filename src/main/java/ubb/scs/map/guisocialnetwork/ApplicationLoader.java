package ubb.scs.map.guisocialnetwork;

import ubb.scs.map.guisocialnetwork.controller.LogInController;
import ubb.scs.map.guisocialnetwork.domain.validators.FriendshipValidator;
import ubb.scs.map.guisocialnetwork.domain.validators.MessageValidator;
import ubb.scs.map.guisocialnetwork.domain.validators.UtilizatorValidator;
import ubb.scs.map.guisocialnetwork.repository.dbrepo.MessageDBRepository;
import ubb.scs.map.guisocialnetwork.repository.dbrepo.ReteaDB;
import ubb.scs.map.guisocialnetwork.repository.dbrepo.UtilizatorDbRepository;
import ubb.scs.map.guisocialnetwork.services.Service;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;

public class ApplicationLoader extends Application {
    private static Service service;

    public static void main(String[] args) {launch(args);}

    @Override
    public void start(Stage primaryStage) throws IOException {
        String username="postgres";
        String pasword="1234";
        String url="jdbc:postgresql://localhost:5432/SocialNetworkDB";
        UtilizatorDbRepository utilizatorRepository =
                new UtilizatorDbRepository(url,username, pasword,  new UtilizatorValidator());
        ReteaDB reteaRepository =
                new ReteaDB(url ,username, pasword,  new FriendshipValidator(), utilizatorRepository);
        MessageDBRepository messageRepository = new MessageDBRepository(url,username, pasword,  new MessageValidator());

        service = new Service(reteaRepository, messageRepository);
        initLogin(primaryStage);
        startMoreSessions(1);
    }

    public static void initLogin(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ApplicationLoader.class.getResource("views/login-signup-user-view.fxml"));

        AnchorPane userLayout = fxmlLoader.load();
        primaryStage.setScene(new Scene(userLayout));

        LogInController logInController = fxmlLoader.getController();
        logInController.setService(service,primaryStage,null);

        primaryStage.setTitle("Sign In Menu");
        primaryStage.setWidth(800);
        primaryStage.show();
    }

    private static void startMoreSessions(int number) throws IOException {
        for(int i = 0; i < number; i++){
            Stage stage = new Stage();
            initLogin(stage);
        }
    }
}