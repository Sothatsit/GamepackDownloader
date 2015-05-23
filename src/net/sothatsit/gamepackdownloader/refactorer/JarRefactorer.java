package net.sothatsit.gamepackdownloader.refactorer;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.descriptor.MethodDescriptor;
import net.sothatsit.gamepackdownloader.descriptor.UnknownDescriptor;
import net.sothatsit.gamepackdownloader.util.JarUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
            while((entry = zis.getNextEntry()) != null) {
                if(!entry.getName().endsWith(".class")) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));

                    int size;
                    byte[] buffer = new byte[2048];
                    while((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        zos.write(buffer, 0, size);
                    }
                    continue;
                }

                bos = new ByteOutputStream();

                int size;
                byte[] buffer = new byte[2048];
                while((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }

                bos.flush();
                bos.close();

                ClassReader reader = new ClassReader(bos.getBytes());

                if(refactorMap.isRemoveClass(reader.getClassName())) {
                    Log.info("Removed Entry \"" + entry.getName() + "\"");
                    bos = null;
                    continue;
                }

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                ClassRefactorer refactorer = new ClassRefactorer(writer, refactorMap);
                reader.accept(refactorer, 0);

                zos.putNextEntry(new ZipEntry(entry.getName()));
                zos.write(writer.toByteArray());

                Log.info("Refactored Entry \"" + entry.getName() + "\"");

                bos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            JarUtil.close(fis, "FileInputStream");
            JarUtil.close(zis, "ZipInputStream");
            JarUtil.close(bos, "ByteOutputStream");
            JarUtil.close(fos, "FileOutputStream");
            if(zos == null) {
                JarUtil.close(zos, "ZipOutputStream");
            }
        }
    }

    private static class ClassRefactorer extends ClassVisitor {

        private RefactorMap refactorMap;
        private ClassVisitor visitor;
        private String className;

        public ClassRefactorer(ClassVisitor visitor, RefactorMap refactorMap) {
            super(Opcodes.ASM5, visitor);

            this.visitor = visitor;
            this.refactorMap = refactorMap;
        }

        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.className = name;

            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();
            String newName = refactorMap.getNewClassName(name);
            String newSuperName = refactorMap.getNewClassName(superName);
            String[] newInterfaces = new String[interfaces.length];

            for(int i=0; i<newInterfaces.length; i++) {
                newInterfaces[i] = refactorMap.getNewClassName(interfaces[i]);
            }

            visitor.visit(version, access, newName, newSignature, newSuperName, newInterfaces);
        }

        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            return new AnnotationRefactorer(visitor.visitAnnotation(newDesc, visible), refactorMap, className);
        }

        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            if(refactorMap.isRemoveField(className, name, desc)) {
                return null;
            }

            String newName = refactorMap.getNewFieldName(className, name);
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();
            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();

            return new FieldRefactorer(visitor.visitField(access, newName, newDesc, newSignature, value), refactorMap, className);
        }

        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            if(refactorMap.isRemoveMethod(className, name, desc)) {
                return null;
            }

            String newName = refactorMap.getNewMethodName(className, name);
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();
            UnknownDescriptor sig = new UnknownDescriptor(signature, refactorMap);
            String newSignature = sig.getWorkingDescriptor();
            String[] newExceptions = null;

            if(exceptions != null) {
                newExceptions = new String[exceptions.length];

                for(int i=0; i<exceptions.length; i++) {
                    newExceptions[i] = refactorMap.getNewClassName(exceptions[i]);
                }
            }

            return new MethodRefactorer(visitor.visitMethod(access, newName, newDesc, newSignature, newExceptions), refactorMap, className);
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
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            return new AnnotationRefactorer(mv.visitAnnotation(newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitParameterAnnotation(int index, String desc, boolean visible) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            return new AnnotationRefactorer(mv.visitParameterAnnotation(index, newDesc, visible), refactorMap, className);
        }

        public AnnotationVisitor visitAnnotationDefault() {
            return new AnnotationRefactorer(mv.visitAnnotationDefault(), refactorMap, className);
        }

        public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            String newOwner = refactorMap.getNewClassName(owner == null ? className : owner);
            String newName = refactorMap.getNewFieldName(owner == null ? className : owner, name);
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            mv.visitFieldInsn(opcode, newOwner, newName, newDesc);
        }

        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            String newOwner = refactorMap.getNewClassName(owner == null ? className : owner);
            String newName = refactorMap.getNewMethodName(owner == null ? className : owner, name);
            MethodDescriptor descriptor = new MethodDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            mv.visitMethodInsn(opcode, newOwner, newName, newDesc, itf);
        }

        public void visitMultiANewArrayInsn(String desc, int dims) {
            FieldDescriptor descriptor = new FieldDescriptor(desc, refactorMap);
            String newDesc = descriptor.getWorkingDescriptor();

            mv.visitMultiANewArrayInsn(newDesc, dims);
        }

        public void visitTypeInsn(int opcode, String type) {
            String newType = refactorMap.getNewClassName(type);

            mv.visitTypeInsn(opcode, newType);
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

            return new AnnotationRefactorer(av.visitAnnotation(name, newDesc), refactorMap, className);
        }

        public void visitEnum(String name, String desc, String value) {
            UnknownDescriptor descriptor = new UnknownDescriptor(desc, refactorMap);

            String newDesc = descriptor.getWorkingDescriptor();

            av.visitEnum(name, newDesc, value);
        }

    }

}
