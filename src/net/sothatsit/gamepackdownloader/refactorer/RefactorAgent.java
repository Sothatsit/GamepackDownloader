package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.agents.DuplicateMethodAgent;
import net.sothatsit.gamepackdownloader.refactorer.agents.JavaKeywordAgent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static List<RefactorAgent> generateAgentList(RefactorMap refactorMap) {
        return new ArrayList<>(Arrays.asList(
                new DuplicateMethodAgent(refactorMap),
                new JavaKeywordAgent(refactorMap)
        ));
    }

}
