package fr.insee.compas.util.observer;

import java.util.*;

import fr.insee.compas.util.observer.listener.IEventListenerObserver;

public class EventObserverManager implements IEventManager {
    private final Map<EventTypeObserver, List<IEventListenerObserver>> listeners =
            new EnumMap<>(EventTypeObserver.class);

    public EventObserverManager() {
        // Noncompliant - method is empty
    }

    public void subscribe(
            EventTypeObserver eventTypeObserver, IEventListenerObserver listenerObserver) {
        listeners.computeIfAbsent(eventTypeObserver, k -> new ArrayList<>()).add(listenerObserver);
    }

    public void unsubscribe(
            EventTypeObserver eventTypeObserver, IEventListenerObserver listenerObserver) {
        List<IEventListenerObserver> eventListeners = listeners.get(eventTypeObserver);
        if (eventListeners != null) {
            eventListeners.remove(listenerObserver);
        }
    }

    public void notifyObservers(EventTypeObserver eventTypeObserver, String data) {
        listeners
                .getOrDefault(eventTypeObserver, List.of())
                .forEach(listener -> listener.update(data));
    }
}
