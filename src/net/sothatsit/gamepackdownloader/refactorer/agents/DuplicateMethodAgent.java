package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import net.sothatsit.gamepackdownloader.refactorer.RefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.ASMUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DuplicateMethodAgent extends RefactorAgent {

    public DuplicateMethodAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode) {
        return !ASMUtil.isInterface(classNode);
    }

    @Override
    public void refactor(ClassNode node) {
        RefactorMap map = getRefactorMap();

        List<MethodNode> valid = new ArrayList<>();
        methods: for(MethodNode method : node.methods) {
            if(ASMUtil.isAbstract(method)) {
                continue;
            }

            for(MethodNode m : valid) {
                if(m.desc.equals(method.desc) && ASMUtil.areSimilar(m.instructions, method.instructions)) {
                    map.setMethodName(node.name, method.name, m.name);
                    map.setRemoveMethod(node.name, method.name, method.desc, true);
                    Log.methodRename(node, method, m.name);
                    Log.methodRemove(node, method, "Duplicate");
                    continue methods;
                }
            }

            valid.add(method);
        }
    }
}
