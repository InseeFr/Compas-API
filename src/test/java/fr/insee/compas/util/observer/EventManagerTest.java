package fr.insee.compas.util.observer;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.insee.compas.util.observer.listener.IEventListenerObserver;

@ExtendWith(MockitoExtension.class)
class EventManagerTest {

    @Mock private IEventListenerObserver mockListener1;

    @Mock private IEventListenerObserver mockListener2;

    @Test
    void testSubscribeAndNotify() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.subscribe(eventType, mockListener1);
        manager.notifyObservers(eventType, testData);

        // Assert
        verify(mockListener1, times(1)).update(testData);
    }

    @Test
    void testUnsubscribe() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.subscribe(eventType, mockListener1);
        manager.unsubscribe(eventType, mockListener1);
        manager.notifyObservers(eventType, testData);

        // Assert
        verify(mockListener1, never()).update(testData);
    }

    @Test
    void testNotifyWithNoListeners() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.notifyObservers(eventType, testData);

        // Assert
        verify(mockListener1, never()).update(testData);
    }

    @Test
    void testMultipleListeners() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.subscribe(eventType, mockListener1);
        manager.subscribe(eventType, mockListener2);
        manager.notifyObservers(eventType, testData);

        // Assert
        verify(mockListener1, times(1)).update(testData);
        verify(mockListener2, times(1)).update(testData);
    }

    @Test
    void testUnsubscribeSpecificListener() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.subscribe(eventType, mockListener1);
        manager.subscribe(eventType, mockListener2);
        manager.unsubscribe(eventType, mockListener1);
        manager.notifyObservers(eventType, testData);

        // Assert
        verify(mockListener1, never()).update(testData);
        verify(mockListener2, times(1)).update(testData);
    }

    @Test
    void testNotifyDifferentEventTypes() {
        // Arrange
        EventObserverManager manager = new EventObserverManager();
        EventTypeObserver eventType1 = EventTypeObserver.EVENT_TYPE_ERROR;
        String testData = "Test data";

        // Act
        manager.subscribe(eventType1, mockListener1);
        manager.notifyObservers(eventType1, testData);

        // Assert
        verify(mockListener1, times(1)).update(testData);
        verify(mockListener2, never()).update(testData);
    }
}
