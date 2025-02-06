package ubb.scs.map.guisocialnetwork.controller;

import javafx.stage.Stage;
import ubb.scs.map.guisocialnetwork.domain.entities.Utilizator;
import ubb.scs.map.guisocialnetwork.services.Service;
import ubb.scs.map.guisocialnetwork.utils.events.UtilizatorEntityChangeEvent;
import ubb.scs.map.guisocialnetwork.utils.observer.Observer;

public interface Controller extends Observer<UtilizatorEntityChangeEvent> {
    void setService(Service service, Stage stage, Utilizator u);
    void setLoggedInUser(Utilizator u);
}
