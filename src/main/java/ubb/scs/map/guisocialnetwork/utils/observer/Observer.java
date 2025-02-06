package ubb.scs.map.guisocialnetwork.utils.observer;
import ubb.scs.map.guisocialnetwork.utils.events.Event;

public interface Observer<E extends Event> {
    void update(E e);
}