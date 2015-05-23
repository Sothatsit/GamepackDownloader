package net.sothatsit.gamepackdownloader.util;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class Log {

    public static void log(String log) {
        System.out.println(log);
    }

    public static void info(String info) {
        log("[info] " + info);
    }

    public static void error(String error) {
        log("[Error] " + error);
    }

    public static void clazz(ClassNode node) {
        log("");
        log("Class \"" + node.name + "\"");
    }

    public static void classRename(ClassNode node, String name) {
        log("Rename Class \"" + node.name + "\" -> \"" + name + "\"");
    }

    public static void fieldRename(ClassNode node, FieldNode field, String name) {
        log(" - Field \"" + field.name + "\" -> \"" + name + "\"");
    }

    public static void methodRename(ClassNode node, MethodNode method, String name) {
        log(" - Method \"" + method.name + "\" -> \"" + name + "\"");
    }

    public static void fieldRemove(ClassNode node, FieldNode field, String reason) {
        log(" - Removed Field \"" + field.name + "\" - " + reason);
    }

    public static void methodRemove(ClassNode node, MethodNode method, String reason) {
        log(" - Removed Method \"" + method.name + "\" - " + reason);
    }

}
