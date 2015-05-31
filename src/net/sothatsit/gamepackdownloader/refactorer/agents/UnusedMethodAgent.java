package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.MultiRefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.List;

public class UnusedMethodAgent extends MultiRefactorAgent {

    private int removedMethods;

    public UnusedMethodAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public void refactorClasses(List<ClassNode> classes) {
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
