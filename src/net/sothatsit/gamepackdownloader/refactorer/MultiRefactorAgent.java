package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class MultiRefactorAgent {

    private RefactorMap refactorMap;

    public MultiRefactorAgent(RefactorMap refactorMap) {
        this.refactorMap = refactorMap;
    }

    public RefactorMap getRefactorMap() {
        return refactorMap;
    }

    public abstract void refactor(List<ClassNode> classes);

    public abstract void logStatistics();

    public abstract void resetStatistics();

}
