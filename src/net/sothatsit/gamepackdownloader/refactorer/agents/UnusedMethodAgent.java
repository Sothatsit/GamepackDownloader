package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.*;
import net.sothatsit.gamepackdownloader.refactorer.MultiRefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.ArrayList;
import java.util.List;

public class UnusedMethodAgent extends MultiRefactorAgent {

    private int removedMethods;

    public UnusedMethodAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public void refactorClasses(List<ClassNode> classes) {
        RefactorMap map = getRefactorMap();

        List<String> referencedMethods = new ArrayList<String>();
        for(ClassNode node : classes) {
            for(MethodNode method : node.methods) {
                for(AbstractInsnNode insn : method.instructions.toArray()) {
                    if(insn instanceof MethodInsnNode) {
                        MethodInsnNode methodInsn = (MethodInsnNode) insn;

                        addIfNotExists(referencedMethods, node.name + " - " + methodInsn.name + methodInsn.desc);
                    } else if(insn instanceof InvokeDynamicInsnNode) {
                        InvokeDynamicInsnNode dynamicInsn = (InvokeDynamicInsnNode) insn;
                        addIfNotExists(referencedMethods, node.name + " - " + dynamicInsn.name + dynamicInsn.desc);
                        addIfNotExists(referencedMethods, dynamicInsn.bsm.getName() + dynamicInsn.bsm.getDesc());
                    }
                }
            }
        }

        for(ClassNode node : classes) {
            for(MethodNode method : node.methods) {
                if(!referencedMethods.contains(node.name + " - " + method.name + method.desc)) {
                    map.setRemoveMethod(node.name, method.name, method.desc, true);
                    Log.methodRemove(node, method, "Method Unused");
                    removedMethods += 1;
                }
            }
        }
    }

    @Override
    public void logStatistics() {
        Log.info("Removed " + removedMethods + " unused methods");
    }

    @Override
    public void resetStatistics() {
        removedMethods = 0;
    }

    public static void addIfNotExists(List<String> list, String element) {
        if(!list.contains(element)) {
            list.add(element);
        }
    }
}
