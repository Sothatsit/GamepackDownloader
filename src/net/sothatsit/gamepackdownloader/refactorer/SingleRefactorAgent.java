package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.agents.DuplicateMethodAgent;
import net.sothatsit.gamepackdownloader.refactorer.agents.JavaKeywordAgent;
import net.sothatsit.gamepackdownloader.refactorer.agents.UpperCamelCaseClassNames;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static List<SingleRefactorAgent> generateAgentList(RefactorMap refactorMap) {
        return new ArrayList<>(Arrays.asList(
                new JavaKeywordAgent(refactorMap),
                new DuplicateMethodAgent(refactorMap),
                new UpperCamelCaseClassNames(refactorMap)
        ));
    }

    public static RefactorMap refactor(List<ClassNode> classes) {
        RefactorMap map = new RefactorMap();
        return refactor(classes, map, generateAgentList(map));
    }

    public static RefactorMap refactor(List<ClassNode> classes, RefactorMap refactorMap, List<SingleRefactorAgent> agents) {
        for(ClassNode node : classes) {
            Log.clazz(node);
            for(SingleRefactorAgent agent : agents) {
                agent.run(node, classes);
            }
        }

        Log.info("");

        for(SingleRefactorAgent agent : agents) {
            agent.logStatistics();
        }

        Log.info("");

        return refactorMap;
    }

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
