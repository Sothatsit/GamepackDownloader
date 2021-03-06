package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.util.List;

public abstract class MultiRefactorAgent extends RefactorAgent {

    private RefactorMap refactorMap;

    public MultiRefactorAgent(RefactorMap refactorMap) {
        this.refactorMap = refactorMap;
    }

    public RefactorMap getRefactorMap() {
        return refactorMap;
    }

    public abstract void refactorClasses(List<ClassNode> classes);

    public boolean isSingle() {
        return false;
    }

    public void runSingle(ClassNode node, List<ClassNode> classes) {
        throw new UnsupportedOperationException("Cannot run single on MultiRefactorAgent");
    }

    public void runMulti(List<ClassNode> classes) {
        refactorClasses(classes);
    }

}
