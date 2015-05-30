package net.sothatsit.gamepackdownloader.util;

public enum LogLevel {

    BASIC,
    SOME,
    ALL;

    public boolean higherThan(LogLevel other) {
        return ordinal() >= other.ordinal();
    }

}
