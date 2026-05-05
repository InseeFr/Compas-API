package fr.insee.compas.util.observer.listener;

import fr.insee.compas.util.observer.EventTypeObserver;

public interface IEventListenerObserver {

    EventTypeObserver getEventType();

    void update(String data);
}
