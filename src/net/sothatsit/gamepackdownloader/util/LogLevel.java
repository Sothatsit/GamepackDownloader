package net.sothatsit.gamepackdownloader.util;

public enum LogLevel {

    BASIC(0),
    DEBUG(1),
    FINE_DEBUG(2);

    private int priority;

    private LogLevel(int priority) {
        this.priority = priority;
    }

    public boolean higherThan(LogLevel other) {
        return priority > other.priority;
    }

}
