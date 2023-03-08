import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class WatcherTest {

    @Test
    void testStart() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();

        // WHEN
        watcher.start();

        // THEN
        assertNotNull(watcher.timer);
        assertNotNull(watcher.task);
        assertEquals(14400L, watcher.period);
    }

    @Test
    void testStop() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();
        watcher.start();

        // WHEN
        watcher.stop();

        // THEN
        assertNull(watcher.timer);
        assertNull(watcher.task);
    }

    @Test
    void testIPMessagesCounter() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();
        CounterManager.setWatchingIPMessagesCunter(-1);
        AtomicBoolean isAlertIPSent = watcher.isAlertIPSent;

        // WHEN
        watcher.start();

        // THEN
        assertTrue(isAlertIPSent.get());
    }

    @Test
    void testSwipMessagesCounter() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();
        CounterManager.setWatchingSwipMessagesCounter(0);
        AtomicBoolean isAlertSwipSent = watcher.isAlertSwipSent;

        // WHEN
        watcher.start();

        // THEN
        assertTrue(isAlertSwipSent.get());
    }

    @Test
    void testIPDSXMessagesCounter() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();
        CounterManager.setWatchingIPMessagesCunter(-1);
        AtomicBoolean isAlertIPDSXSent = watcher.isAlertIPDSXSent;

        // WHEN
        watcher.start();

        // THEN
        assertTrue(isAlertIPDSXSent.get());
    }
}

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Timer;
import java.util.TimerTask;

import static org.mockito.Mockito.*;

class WatcherTest {

    @Test
    void testWatcherTask() {
        // GIVEN
        Watcher watcher = Watcher.getInstance();
        CounterManager.setWatchingIPMessagesCunter(-1);

        // Créez un mock pour le TimerTask
        TimerTask taskMock = mock(TimerTask.class);

        // Simuler le temps qui s'écoule de 4 heures
        long fourHoursInMillis = 4 * 60 * 60 * 1000L;
        Mockito.doAnswer(invocation -> {
            // Appelez la méthode run() du TimerTask simulée
            taskMock.run();
            return null;
        }).when(taskMock).scheduledExecutionTime();

        // Configurez le Timer pour utiliser le mock TimerTask
        Timer timerMock = mock(Timer.class);
        doReturn(timerMock).when(watcher).getTimer();
        doReturn(taskMock).when(watcher).getTask();

        // WHEN
        watcher.start();

        // Attendre que le TimerTask soit exécuté
        try {
            Thread.sleep(fourHoursInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // THEN
        // Vérifiez que la méthode run() du TimerTask a été appelée
        verify(taskMock, times(1)).run();
    }
}
package org.example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Timer;
import java.util.TimerTask;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WatcherTest {

    @Test
    public void testWatcherRun() {
        // Création des objets simulés
        CounterManager counterManager = mock(CounterManager.class);
        RsLogEnum rsLogEnum = mock(RsLogEnum.class);

        // Création de l'objet à tester
        Watcher watcher = Watcher.getInstance();
        watcher.period = 100L; // Période courte pour le test
        watcher.task = Mockito.spy(watcher.new WatcherTask(counterManager, rsLogEnum));

        // Exécution de la méthode à tester
        watcher.task.run();

        // Vérification que les méthodes attendues ont été appelées
        verify(counterManager).getWatchingIPMessagesCunter();
        verify(counterManager).getWatchingSwipessagesCounter();
        verify(counterManager).getWatchingIPDSXMessagesCunter();
        verify(rsLogEnum).ICO_MESSAGE.IP_DOWN.generateLog(anyString());
        verify(rsLogEnum).ICO_MESSAGE.SWIP_DOWN.generateLog(anyString());
        verify(rsLogEnum).ICO_MESSAGE.IPDSX_DOWN.generateLog(anyString());
        verify(counterManager).resetwatching();
    }
}

