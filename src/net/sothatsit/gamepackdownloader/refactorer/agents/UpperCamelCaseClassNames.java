package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.SingleRefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.List;

public class UpperCamelCaseClassNames extends SingleRefactorAgent {

    private int renamedClasses;

    public UpperCamelCaseClassNames(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode, List<ClassNode> classes) {
        return Character.toUpperCase(classNode.name.charAt(0)) != classNode.name.charAt(0);
    }

    @Override
    public void refactor(ClassNode node, List<ClassNode> classes) {
        RefactorMap map = getRefactorMap();

        String newName = Character.toUpperCase(node.name.charAt(0)) + node.name.substring(1);

        map.setClassName(node.name, newName);
        Log.classRename(node, newName);
        renamedClasses += 1;
    }

    @Override
    public void logStatistics() {
        Log.info("Renamed " + renamedClasses + " classes to have UpperCamelCase names");
    }

    @Override
    public void resetStatistics() {
        renamedClasses = 0;
    }
}
