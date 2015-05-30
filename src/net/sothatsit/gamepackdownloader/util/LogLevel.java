package net.sothatsit.gamepackdownloader.util;

public enum LogLevel {

    BASIC,
    DEBUG,
    FINE_DEBUG;

    public boolean higherThan(LogLevel other) {
        return ordinal() >= other.ordinal();
    }

}
