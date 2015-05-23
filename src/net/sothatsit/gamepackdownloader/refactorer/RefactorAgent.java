package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;

public abstract class RefactorAgent {

    private RefactorMap refactorMap;

    public RefactorAgent(RefactorMap refactorMap) {
        this.refactorMap = refactorMap;
    }

    public RefactorMap getRefactorMap() {
        return refactorMap;
    }

    public final void run(ClassNode classNode) {
        if(accept(classNode)) {
            refactor(classNode);
        }
    }

    public abstract boolean accept(ClassNode classNode);

    public abstract void refactor(ClassNode classNode);

}
