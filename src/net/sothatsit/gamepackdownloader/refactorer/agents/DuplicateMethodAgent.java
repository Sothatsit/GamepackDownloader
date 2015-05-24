package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import net.sothatsit.gamepackdownloader.refactorer.RefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.ASMUtil;
import net.sothatsit.gamepackdownloader.util.Log;
import net.sothatsit.gamepackdownloader.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class DuplicateMethodAgent extends RefactorAgent {

    public DuplicateMethodAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode, List<ClassNode> classes) {
        return !ASMUtil.isInterface(classNode);
    }

    @Override
    public void refactor(ClassNode node, List<ClassNode> classes) {
        RefactorMap map = getRefactorMap();

        List<MethodNode> valid = new ArrayList<>();
        methods: for(MethodNode method : node.methods) {
            if(ASMUtil.isAbstract(method)) {
                continue;
            }

            if(ASMUtil.findSuperMethod(node, method, classes) != null) {
                continue;
            }

            List<ClassNode> subClasses = ASMUtil.findSubClasses(node, classes);

            for(MethodNode m : valid) {
                if(m.desc.equals(method.desc) && ASMUtil.areSimilar(m.instructions, method.instructions)) {
                    List<Pair<MethodNode, ClassNode>> validSub = new ArrayList<>();
                    List<Pair<MethodNode, ClassNode>> methodSub = new ArrayList<>();

                    for(ClassNode sub : subClasses) {
                        MethodNode subMethod = ASMUtil.findMethod(sub, m.name, m.desc);

                        if(subMethod != null) {
                            validSub.add(new Pair<>(subMethod, sub));
                        }
                    }

                    for(ClassNode sub : subClasses) {
                        MethodNode subMethod = ASMUtil.findMethod(sub, method.name, method.desc);

                        if(subMethod != null) {
                            methodSub.add(new Pair<>(subMethod, sub));
                        }
                    }

                    List<Pair<MethodNode, MethodNode>> paired = new ArrayList<>();
                    methodSubIter: for(Pair<MethodNode, ClassNode> pair1 : methodSub) {
                        MethodNode subMethod = pair1.getObj1();
                        ClassNode subMethodClass = pair1.getObj2();

                        for(Pair<MethodNode, ClassNode> pair2 : validSub) {
                            MethodNode subValid = pair2.getObj1();
                            ClassNode subValidClass = pair2.getObj2();

                            if(subMethodClass.name.equals(subValidClass.name)) {
                                paired.add(new Pair<>(subMethod, subValid));
                                continue methodSubIter;
                            }
                        }

                        continue methods;
                    }

                    for(Pair<MethodNode, MethodNode> pair : paired) {
                        MethodNode m1 = pair.getObj1();
                        MethodNode m2 = pair.getObj2();

                        if(!m1.desc.equals(m2.desc) || !ASMUtil.areSimilar(m1.instructions, m2.instructions)) {
                            continue methods;
                        }
                    }

                    map.setMethodName(node.name, method.name, m.name);
                    map.setRemoveMethod(node.name, method.name, method.desc, true);
                    Log.methodRename(node, method, m.name);
                    Log.methodRemove(node, method, "Duplicate");

                    for(ClassNode sub : subClasses) {
                        MethodNode subMethod = ASMUtil.findMethod(sub, method.name, method.desc);

                        if(subMethod != null) {
                            map.setMethodName(sub.name, subMethod.name, m.name);
                            map.setRemoveMethod(sub.name, subMethod.name, subMethod.desc, true);
                        }
                    }

                    continue methods;
                }
            }

            valid.add(method);
        }
    }
}
