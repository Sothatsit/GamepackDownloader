package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.RefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;

import java.util.List;

public class UpperCamelCaseClassNames extends RefactorAgent {

    public UpperCamelCaseClassNames(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode, List<ClassNode> classes) {
        return classNode.name.length() > 2 && Character.toUpperCase(classNode.name.charAt(0)) != classNode.name.charAt(0);
    }

    @Override
    public void refactor(ClassNode node, List<ClassNode> classes) {
        RefactorMap map = getRefactorMap();

        map.setClassName(node.name, Character.toUpperCase(node.name.charAt(0)) + node.name.substring(1));
    }
}
