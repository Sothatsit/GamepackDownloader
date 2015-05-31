package net.sothatsit.gamepackdownloader.refactorer;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.descriptor.MethodDescriptor;
import net.sothatsit.gamepackdownloader.descriptor.UnknownDescriptor;
import net.sothatsit.gamepackdownloader.util.ASMUtil;
import net.sothatsit.gamepackdownloader.util.JarUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class JarRefactorer {

    public static void refactor(File input, File output, RefactorMap refactorMap) {
        FileInputStream fis = null;
        JarInputStream zis = null;
        ByteOutputStream bos = null;
        FileOutputStream fos = null;
        JarOutputStream zos = null;

        try {
            fis = new FileInputStream(input);
            zis = new JarInputStream(fis);
            fos = new FileOutputStream(output);
            zos = new JarOutputStream(fos);

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.getName().endsWith(".class")) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));

                    int size;
                    byte[] buffer = new byte[2048];
                    while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        zos.write(buffer, 0, size);
                    }
                    continue;
                }

                bos = new ByteOutputStream();

                int size;
                byte[] buffer = new byte[2048];
                while ((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }

                bos.flush();
                bos.close();

                ClassReader reader = new ClassReader(bos.getBytes());

                if (refactorMap.isRemoveClass(reader.getClassName())) {
                    Log.info("Removed Entry \"" + entry.getName() + "\"");
                    bos = null;
                    continue;
                }

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                ClassRefactorer refactorer = new ClassRefactorer(writer, refactorMap);
                reader.accept(refactorer, 0);

                zos.putNextEntry(new ZipEntry(refactorMap.getNewClassName(reader.getClassName()) + ".class"));
                zos.write(writer.toByteArray());

                Log.refactoredEntry(entry.getName());

                bos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            JarUtil.close(fis, "FileInputStream");
            JarUtil.close(zis, "ZipInputStream");
            JarUtil.close(bos, "ByteOutputStream");
            JarUtil.close(zos, "ZipOutputStream");
        }
    }

    private static class ClassRefactorer extends ClassVisitor {

        private RefactorMap refactorMap;
        // private ClassVisitor visitor;
        private String className;

        public ClassRefactorer(ClassVisitor visitor, RefactorMap refactorMap) {
            super(Opcodes.ASM5, visitor);

            //this.visitor = visitor;
            this.refactorMap = refactorMap;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;

            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();
            String newName = refactorMap.getNewClassName(name);
            String newSuperName = refactorMap.getNewClassName(superName);
            String[] newInterfaces = new String[interfaces.length];

            for (int i = 0; i < newInterfaces.length; i++) {
                newInterfaces[i] = refactorMap.getNewClassName(interfaces[i]);
            }

            String neww = getMethodString("visit", version, ASMUtil.makePublic(access), newName, newSignature, newSuperName, newInterfaces);
            String orig = getMethodString("     orig", version, access, name, signature, superName, interfaces);
            Log.fineDebug(neww + orig);

            cv.visit(version, ASMUtil.makePublic(access), newName, newSignature, newSuperName, newInterfaces);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitAnnotation", newDesc, visible);
            String orig = getMethodString("     orig", desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(cv.visitAnnotation(newDesc, visible), refactorMap, className);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if (refactorMap.isRemoveField(className, name, desc)) {
                return null;
            }

            String newName = refactorMap.getNewFieldName(className, name);
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();
            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();

            String neww = getMethodString("visitField", ASMUtil.makePublic(access), newName, newDesc, newSignature, value);
            String orig = getMethodString("     orig", access, name, desc, signature, value);
            Log.fineDebug(neww + orig);

            return new FieldRefactorer(cv.visitField(ASMUtil.makePublic(access), newName, newDesc, newSignature, value), refactorMap, className);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if (refactorMap.isRemoveMethod(className, name, desc)) {
                return null;
            }

            String newName = refactorMap.getNewMethodName(className, name);
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();
            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();
            String[] newExceptions = null;

            if (exceptions != null) {
                newExceptions = new String[exceptions.length];

                for (int i = 0; i < exceptions.length; i++) {
                    newExceptions[i] = refactorMap.getNewClassName(exceptions[i]);
                }
            }

            String neww = getMethodString("visitMethod", ASMUtil.makePublic(access), newName, newDesc, newSignature, newExceptions);
            String orig = getMethodString("     orig", access, name, desc, signature, exceptions);
            Log.fineDebug(neww + orig);

            return new MethodRefactorer(cv.visitMethod(ASMUtil.makePublic(access), newName, newDesc, newSignature, newExceptions), refactorMap, className);
        }

        public void visitInnerClass(String name, String outerName, String innerName, int access) {
            String neww = getMethodString("visitInnerClass", name, (outerName == null ? null : refactorMap.getNewClassName(outerName)), innerName, access);
            String orig = getMethodString("     orig", name, outerName, innerName, access);
            Log.fineDebug(neww + orig);

            cv.visitInnerClass(name, (outerName == null ? null : refactorMap.getNewClassName(outerName)), innerName, access);
        }

        public void visitOuterClass(String owner, String name, String desc) {
            String newOwner = refactorMap.getNewClassName(owner);
            String newName = (name == null ? null : refactorMap.getNewMethodName(owner, name));
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitOuterClass", newOwner, newName, newDesc);
            String orig = getMethodString("     orig", owner, name, desc);
            Log.fineDebug(neww + orig);

            cv.visitOuterClass(newOwner, newName, newDesc);
        }

    }

    private static class MethodRefactorer extends MethodVisitor {

        private RefactorMap refactorMap;
        //private MethodVisitor visitor;
        private String className;

        public MethodRefactorer(MethodVisitor visitor, RefactorMap refactorMap, String className) {
            super(Opcodes.ASM5, visitor);

            //this.visitor = visitor;
            this.refactorMap = refactorMap;
            this.className = className;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitAnnotation", newDesc, visible);
            String orig = getMethodString("     orig", desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitAnnotation(newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitInsnAnnotation", typeRef, typePath, newDesc, visible);
            String orig = getMethodString("     orig", typeRef, typePath, desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitInsnAnnotation(typeRef, typePath, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitLocalVariableAnnotation", typeRef, typePath, start, end, index, newDesc, visible);
            String orig = getMethodString("     orig", typeRef, typePath, start, end, index, desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitTryCatchAnnotation", typeRef, typePath, newDesc, visible);
            String orig = getMethodString("     orig", typeRef, typePath, desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitTryCatchAnnotation(typeRef, typePath, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitTypeAnnotation", typeRef, typePath, newDesc, visible);
            String orig = getMethodString("     orig", typeRef, typePath, desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitTypeAnnotation(typeRef, typePath, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitParameterAnnotation(int index, String desc, boolean visible) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitParameterAnnotation", index, newDesc, visible);
            String orig = getMethodString("     orig", index, desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitParameterAnnotation(index, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitAnnotationDefault() {
            String neww = getMethodString("visitAnnotationDefault");
            String orig = getMethodString("     orig");
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(mv.visitAnnotationDefault(), refactorMap, className);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            String newOwner = refactorMap.getNewClassName(owner == null ? className : owner);
            String newName = refactorMap.getNewFieldName(owner == null ? className : owner, name);
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitFieldInsn", opcode, newOwner, newName, newDesc);
            String orig = getMethodString("     orig", opcode, owner, name, desc);
            Log.fineDebug(neww + orig);

            mv.visitFieldInsn(opcode, newOwner, newName, newDesc);
        }

        public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
            String newName = refactorMap.getNewMethodName(className, name);
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            String newBsmOwner = refactorMap.getNewClassName(bsm.getOwner());
            String newBsmName = refactorMap.getNewMethodName(bsm.getOwner(), bsm.getName());
            MethodDescriptor bsmDescriptor = new MethodDescriptor(bsm.getDesc(), refactorMap);
            String newBsmDesc = bsmDescriptor.getWorkingDescriptor();
            Handle newBsm = new Handle(bsm.getTag(), newBsmOwner, newBsmName, newBsmDesc);

            String neww = getMethodString("visitInvokeDynamicInsn", newName, newDesc, newBsm, bsmArgs);
            String orig = getMethodString("     orig", name, desc, bsm, bsmArgs);
            Log.fineDebug(neww + orig);

            mv.visitInvokeDynamicInsn(newName, newDesc, newBsm, bsmArgs);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            String newOwner = refactorMap.getNewClassName(owner == null ? className : owner);
            String newName = refactorMap.getNewMethodName(owner == null ? className : owner, name);
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitMethodInsn", opcode, newOwner, newName, newDesc, itf);
            String orig = getMethodString("     orig", opcode, owner, name, desc, itf);
            Log.fineDebug(neww + orig);

            mv.visitMethodInsn(opcode, newOwner, newName, newDesc, itf);
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitMultiANewArrayInsn", newDesc, dims);
            String orig = getMethodString("     orig", desc, dims);
            Log.fineDebug(neww + orig);

            mv.visitMultiANewArrayInsn(newDesc, dims);
        }

        public void visitTypeInsn(int opcode, String type) {
            String newType = refactorMap.getNewClassName(type);

            String neww = getMethodString("visitTypeInsn", opcode, newType);
            String orig = getMethodString("     orig", opcode, type);
            Log.fineDebug(neww + orig);

            mv.visitTypeInsn(opcode, newType);
        }

        public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
            String newName = refactorMap.getNewFieldName(className, name);
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();
            UnknownDescriptor descriptor2 = new UnknownDescriptor(signature, refactorMap);
            String newSig = descriptor2.getWorkingDescriptor();

            String neww = getMethodString("visitLocalVariable", newName, newDesc, newSig, start, end, index);
            String orig = getMethodString("     orig", name, desc, signature, start, end, index);
            Log.fineDebug(neww + orig);

            mv.visitLocalVariable(newName, newDesc, newSig, start, end, index);
        }

        public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
            String neww = getMethodString("visitTryCatchBlock", start, end, handler, (type == null ? null : refactorMap.getClassName(type)));
            String orig = getMethodString("     orig", start, end, handler, type);
            Log.fineDebug(neww + orig);

            mv.visitTryCatchBlock(start, end, handler, (type == null ? null : refactorMap.getClassName(type)));
        }

    }

    private static class FieldRefactorer extends FieldVisitor {

        private RefactorMap refactorMap;
        //private FieldVisitor visitor;
        private String className;

        public FieldRefactorer(FieldVisitor visitor, RefactorMap refactorMap, String className) {
            super(Opcodes.ASM5, visitor);

            //this.visitor = visitor;
            this.refactorMap = refactorMap;
            this.className = className;
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitAnnotation", newDesc, visible);
            String orig = getMethodString("     orig", desc, visible);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(fv.visitAnnotation(newDesc, visible), refactorMap, className);
        }

    }

    private static class AnnotationRefactorer extends AnnotationVisitor {

        private RefactorMap refactorMap;
        //private AnnotationVisitor visitor;
        private String className;

        public AnnotationRefactorer(AnnotationVisitor visitor, RefactorMap refactorMap, String className) {
            super(Opcodes.ASM5, visitor);

            //this.visitor = visitor;
            this.refactorMap = refactorMap;
            this.className = className;
        }

        public AnnotationVisitor visitAnnotation(String name, String desc) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitAnnotation", name, newDesc);
            String orig = getMethodString("     orig", name, desc);
            Log.fineDebug(neww + orig);

            return new AnnotationRefactorer(av.visitAnnotation(name, newDesc), refactorMap, className);
        }

        public void visitEnum(String name, String desc, String value) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            String neww = getMethodString("visitEnum", name, newDesc, value);
            String orig = getMethodString("     orig", name, desc, value);
            Log.fineDebug(neww + orig);

            av.visitEnum(name, newDesc, value);
        }

    }

    public static String getMethodString(String method, Object... arguments) {
        StringBuilder builder = new StringBuilder();

        builder.append(method);
        builder.append('(');

        boolean first = true;
        for (Object arg : arguments) {
            if (!first) {
                builder.append(", ");
            } else {
                first = false;
            }

            builder.append(objToString(arg));
        }

        builder.append(')');

        return builder.toString();
    }

    public static String objToString(Object obj) {
        if (obj == null) {
            return "null";
        } else if (obj.getClass().isArray()) {
            return arrayToString(obj);
        } else {
            return obj.toString();
        }
    }

    public static String arrayToString(Object obj) {
        if (obj == null || !obj.getClass().isArray()) {
            return "not array";
        }

        StringBuilder builder = new StringBuilder();

        builder.append('[');

        boolean first = true;
        for (int i = 0; i < Array.getLength(obj); i++) {
            if (!first) {
                builder.append(", ");
            } else {
                first = false;
            }

            Object element = Array.get(obj, i);

            builder.append(element);
        }

        builder.append(']');

        return builder.toString();
    }

}
