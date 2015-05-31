package net.sothatsit.gamepackdownloader.util;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class Log {

    private static LogLevel logLevel;

    public static LogLevel getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(LogLevel logLevel) {
        Log.logLevel = logLevel;
    }

    public static void log(String log) {
        System.out.println(log);
    }

    public static void info(String info) {
        log("[info] " + info);
    }

    public static void error(String error) {
        log("[Error] " + error);
    }

    public static void fineDebug(String debug) {
        if(logLevel.lowerThan(LogLevel.FINE_DEBUG)) {
            return;
        }

        log(debug);
    }

    public static void debug(String debug) {
        if(logLevel.lowerThan(LogLevel.DEBUG)) {
            return;
        }

        log(debug);
    }

    public static void clazz(ClassNode node) {
        debug("");
        debug("Checking Class \"" + node.name + "\"");
    }

    public static void classRename(ClassNode node, String name) {
        debug("Rename Class \"" + node.name + "\" -> \"" + name + "\"");
    }

    public static void fieldRename(ClassNode node, FieldNode field, String name) {
        debug(" - Field \"" + field.name + "\" -> \"" + name + "\"");
    }

    public static void methodRename(ClassNode node, MethodNode method, String name) {
        debug(" - Method \"" + method.name + "\" -> \"" + name + "\"");
    }

    public static void fieldRemove(ClassNode node, FieldNode field, String reason) {
        debug(" - Removed Field \"" + field.name + "\" - " + reason);
    }

    public static void methodRemove(ClassNode node, MethodNode method, String reason) {
        debug(" - Removed Method \"" + method.name + "\" - " + reason);
    }

    public static void loadedEntry(String name) {
        debug("Loaded Entry \"" + name + "\"");
    }

    public static void refactoredEntry(String name) {
        debug("Refactored Entry \"" + name + "\"");
    }

}
