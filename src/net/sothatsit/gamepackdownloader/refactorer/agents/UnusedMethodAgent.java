package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.RefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.List;

public class UnusedMethodAgent extends RefactorAgent {

    public static final String SUFFIX = "_key";

    private int removedMethods;

    public UnusedMethodAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode, List<ClassNode> classes) {
        return false;
    }

    @Override
    public void refactor(ClassNode node, List<ClassNode> classes) {
        //RefactorMap map = getRefactorMap();
    }

    @Override
    public void logStatistics() {
        Log.info("Removed " + removedMethods + " unused methods");
    }

    @Override
    public void resetStatistics() {
        removedMethods = 0;
    }
}
