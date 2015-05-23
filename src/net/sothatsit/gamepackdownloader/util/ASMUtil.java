package net.sothatsit.gamepackdownloader.util;

import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;

public class ASMUtil {

    public static boolean isStatic(MethodNode method) {
        return (method.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isStatic(FieldNode field) {
        return (field.access & Opcodes.ACC_STATIC) != 0;
    }

    public static boolean isAbstract(ClassNode clazz) {
        return (clazz.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isAbstract(MethodNode method) {
        return (method.access & Opcodes.ACC_ABSTRACT) != 0;
    }

    public static boolean isInterface(ClassNode clazz) {
        return (clazz.access & Opcodes.ACC_INTERFACE) != 0;
    }

}
