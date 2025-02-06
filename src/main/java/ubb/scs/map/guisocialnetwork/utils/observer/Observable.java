package ubb.scs.map.guisocialnetwork.utils.observer;
import ubb.scs.map.guisocialnetwork.utils.events.Event;

public interface Observable<E extends Event> {
    void addObserver(Observer<E> e);
    void removeObserver(Observer<E> e);
    void notifyObservers(E t);
}
