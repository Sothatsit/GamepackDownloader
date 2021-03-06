package net.sothatsit.gamepackdownloader.refactorer.agents;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import jdk.internal.org.objectweb.asm.tree.FieldNode;
import jdk.internal.org.objectweb.asm.tree.MethodNode;
import net.sothatsit.gamepackdownloader.refactorer.SingleRefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.JavaUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.util.List;

public class JavaKeywordAgent extends SingleRefactorAgent {

    public static final String SUFFIX = "_key";

    private int renamedClasses;
    private int renamedFields;
    private int renamedMethods;

    public JavaKeywordAgent(RefactorMap refactorMap) {
        super(refactorMap);
        resetStatistics();
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
            renamedClasses += 1;
        }

        for(FieldNode field : node.fields) {
            if(JavaUtil.isJavaKeyword(field.name)) {
                map.setFieldName(node.name, field.name, field.name + SUFFIX);
                Log.fieldRename(node, field, field.name + SUFFIX);

                renamedFields += 1;
            }
        }

        for(MethodNode method : node.methods) {
            if(JavaUtil.isJavaKeyword(method.name)) {
                map.setMethodName(node.name, method.name, method.name + SUFFIX);
                Log.methodRename(node, method, method.name + SUFFIX);

                renamedMethods += 1;
            }
        }
    }

    @Override
    public void logStatistics() {
        Log.info("Renamed " + renamedClasses + " classes, " + renamedFields + " fields and " + renamedMethods + " methods for having java keywords in their names");
    }

    @Override
    public void resetStatistics() {
        renamedClasses = 0;
        renamedFields = 0;
        renamedMethods = 0;
    }
}
