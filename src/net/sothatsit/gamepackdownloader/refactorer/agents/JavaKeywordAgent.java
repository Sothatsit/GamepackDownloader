package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import net.sothatsit.gamepackdownloader.refactorer.RefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.JavaUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.List;

public class JavaKeywordAgent extends RefactorAgent {

    public static final String SUFFIX = "_key";

    public JavaKeywordAgent(RefactorMap refactorMap) {
        super(refactorMap);
    }

    @Override
    public boolean accept(ClassNode classNode, List<ClassNode> classes) {
        return true;
    }

    @Override
    public void refactor(ClassNode node, List<ClassNode> classes) {
        RefactorMap map = getRefactorMap();

        if(JavaUtil.isJavaKeyword(node.name)) {
            map.setClassName(node.name, node.name + SUFFIX);
            Log.classRename(node, node.name + SUFFIX);
        }

        for(FieldNode field : node.fields) {
            if(JavaUtil.isJavaKeyword(field.name)) {
                map.setFieldName(node.name, field.name, field.name + SUFFIX);
                Log.fieldRename(node, field, field.name + SUFFIX);
            }
        }

        for(MethodNode method : node.methods) {
            if(JavaUtil.isJavaKeyword(method.name)) {
                map.setMethodName(node.name, method.name, method.name + SUFFIX);
                Log.methodRename(node, method, method.name + SUFFIX);
            }
        }
    }
}
