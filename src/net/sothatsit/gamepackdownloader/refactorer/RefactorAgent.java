package net.sothatsit.gamepackdownloader.refactorer;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.agents.DuplicateMethodAgent;
import net.sothatsit.gamepackdownloader.refactorer.agents.JavaKeywordAgent;
import net.sothatsit.gamepackdownloader.refactorer.agents.UpperCamelCaseClassNames;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class RefactorAgent {

    public abstract boolean isSingle();

    public abstract void runSingle(ClassNode node, List<ClassNode> classes);

    public abstract void runMulti(List<ClassNode> classes);

    public abstract void logStatistics();

    public abstract void resetStatistics();

    public static List<RefactorAgent> generateAgentList(RefactorMap refactorMap) {
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

    public static RefactorMap refactor(List<ClassNode> classes, RefactorMap refactorMap, List<RefactorAgent> agents) {
        for(RefactorAgent agent : agents) {
            if(agent.isSingle()) {
                for(ClassNode node : classes) {
                    agent.runSingle(node, classes);
                }
            } else {
                agent.runMulti(classes);
            }
        }

        Log.info("");

        for(RefactorAgent agent : agents) {
            //agent.logStatistics();
        }

        Log.info("");

        return refactorMap;
    }

}
