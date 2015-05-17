package net.sothatsit.gamepackdownloader.refactor.asm;

import jdk.internal.org.objectweb.asm.ClassVisitor;
import jdk.internal.org.objectweb.asm.FieldVisitor;
import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import net.sothatsit.gamepackdownloader.refactor.RefactorMap;
import net.sothatsit.gamepackdownloader.refactor.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.refactor.descriptor.MethodDescriptor;

public class ClassRefactorer extends ClassVisitor {

    private ClassVisitor visitor;
    private RefactorMap refactorMap;

    public ClassRefactorer(ClassVisitor visitor, RefactorMap refactorMap) {
        super(Opcodes.ASM4);

        this.visitor = visitor;
        this.refactorMap = refactorMap;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        String newName = refactorMap.getNewClassName(name);
        String newSuperName = refactorMap.getNewClassName(superName);
        String[] newInterfaces = new String[interfaces.length];

        for(int i=0; i < interfaces.length; i++) {
            newInterfaces[i] = refactorMap.getNewClassName(interfaces[i]);
        }

        System.out.println("Class signature: " + signature);

        visitor.visit(version, access, newName, signature, newSuperName, newInterfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
        String newDesc = descriptor.getWorkingDescriptor();

        String[] newExceptions = null;

        if(exceptions != null) {
            newExceptions = new String[exceptions.length];

            for(int i=0; i < exceptions.length; i++) {
                newExceptions[i] = refactorMap.getNewClassName(exceptions[i]);
            }
        }

        System.out.println("Method signature: " + signature);

        return new MethodRefactorer(visitor.visitMethod(access, name, newDesc, signature, newExceptions), refactorMap);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
        String newDesc = descriptor.getWorkingDescriptor();

        System.out.println("Field signature: " + signature);

        return visitor.visitField(access, name, newDesc, signature, value);
    }
}
