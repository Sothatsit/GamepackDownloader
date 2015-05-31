package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class SingleRefactorAgent extends RefactorAgent {

    private RefactorMap refactorMap;

    public SingleRefactorAgent(RefactorMap refactorMap) {
        this.refactorMap = refactorMap;
    }

    public RefactorMap getRefactorMap() {
        return refactorMap;
    }

    public final void run(ClassNode classNode, List<ClassNode> classes) {
        if (accept(classNode, classes)) {
            refactor(classNode, classes);
        }
    }

    public abstract boolean accept(ClassNode classNode, List<ClassNode> classes);

    public abstract void refactor(ClassNode classNode, List<ClassNode> classes);

    public abstract void logStatistics();

    public abstract void resetStatistics();

    public boolean isSingle() {
        return true;
    }

    public void runSingle(ClassNode node, List<ClassNode> classes) {
        run(node, classes);
    }

    public void runMulti(List<ClassNode> classes) {
        throw new UnsupportedOperationException("Cannot run multi on SingleRefactorAgent");
    }

}
