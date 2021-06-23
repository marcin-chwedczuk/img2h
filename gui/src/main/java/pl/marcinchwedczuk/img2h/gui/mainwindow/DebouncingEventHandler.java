package pl.marcinchwedczuk.img2h.gui.mainwindow;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;

import java.time.Duration;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

public class DebouncingEventHandler<T extends Event>
        implements EventHandler<T> {

    private static Timer timer = new Timer("debouncing-event-handler-timer", true);

    private final AtomicInteger pendingOperations = new AtomicInteger(0);
    private final Duration waitTime;
    private final EventHandler<T> inner;

    public DebouncingEventHandler(Duration waitTime, EventHandler<T> inner) {
        this.waitTime = Objects.requireNonNull(waitTime);
        this.inner = Objects.requireNonNull(inner);
    }

    @Override
    public void handle(T event) {
        int pendingOperations = this.pendingOperations.incrementAndGet();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (DebouncingEventHandler.this.pendingOperations.get() == pendingOperations) {
                    // No new operation was added
                    Platform.runLater(() -> inner.handle(event));
                }
            }
        }, waitTime.toMillis());
    }
}
