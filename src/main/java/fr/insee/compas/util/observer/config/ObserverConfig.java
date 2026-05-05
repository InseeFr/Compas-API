package fr.insee.compas.util.observer.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fr.insee.compas.util.observer.EventObserverManager;
import fr.insee.compas.util.observer.IEventManager;
import fr.insee.compas.util.observer.listener.IEventListenerObserver;

@Configuration
public class ObserverConfig {

    @Bean
    public IEventManager eventObserverManager(List<IEventListenerObserver> listeners) {
        IEventManager manager = new EventObserverManager();
        listeners.forEach(l -> manager.subscribe(l.getEventType(), l));
        return manager;
    }
}
