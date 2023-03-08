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
