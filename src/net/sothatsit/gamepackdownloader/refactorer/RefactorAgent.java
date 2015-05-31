package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.List;

public interface RefactorAgent {

    public boolean isSingle();

    public void runSingle(ClassNode node, List<ClassNode> classes);

    public void runMulti(List<ClassNode> classes);

}
