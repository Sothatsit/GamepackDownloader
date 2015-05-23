package net.sothatsit.gamepackdownloader.refactor.asm;

import jdk.internal.org.objectweb.asm.MethodVisitor;
import jdk.internal.org.objectweb.asm.Opcodes;
import net.sothatsit.gamepackdownloader.rename.RefactorMap;
import net.sothatsit.gamepackdownloader.refactor.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.refactor.descriptor.MethodDescriptor;

public class MethodRefactorer extends MethodVisitor {

    private MethodVisitor visitor;
    private RefactorMap refactorMap;

    public MethodRefactorer(MethodVisitor visitor, RefactorMap refactorMap) {
        super(Opcodes.ASM4, visitor);

        this.visitor = visitor;
        this.refactorMap = refactorMap;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itF) {
        MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
        String newDesc = descriptor.getWorkingDescriptor();

        visitor.visitMethodInsn(opcode, owner, name, newDesc, itF);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
        String newDesc = descriptor.getWorkingDescriptor();

        visitor.visitFieldInsn(opcode, owner, name, newDesc);
    }

}
