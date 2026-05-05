package fr.insee.compas.util.observer;

import fr.insee.compas.util.observer.listener.IEventListenerObserver;

public interface IEventManager {
    void subscribe(EventTypeObserver eventTypeObserver, IEventListenerObserver listenerObserver);

    void unsubscribe(EventTypeObserver eventTypeObserver, IEventListenerObserver listenerObserver);

    void notifyObservers(EventTypeObserver eventTypeObserver, String data);
}
