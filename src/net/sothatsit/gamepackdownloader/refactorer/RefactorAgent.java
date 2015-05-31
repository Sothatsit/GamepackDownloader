package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class RefactorAgent {

    public abstract boolean isSingle();

    public abstract void runSingle(ClassNode node, List<ClassNode> classes);

    public abstract void runMulti(List<ClassNode> classes);

}
