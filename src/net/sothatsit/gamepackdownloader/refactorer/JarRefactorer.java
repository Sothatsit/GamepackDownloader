package net.sothatsit.gamepackdownloader.refactorer;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.util.JarUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarRefactorer {

    public static void refactor(File input, File output, RefactorMap refactorMap) {
        FileInputStream fis = null;
        ZipInputStream zis = null;
        ByteOutputStream bos = null;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        try {
            fis = new FileInputStream(input);
            zis = new ZipInputStream(fis);
            fos = new FileOutputStream(output);
            zos = new ZipOutputStream(fos);

            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                if(!entry.getName().endsWith(".class")) {
                    int size;
                    byte[] buffer = new byte[2048];
                    while((size = zis.read(buffer, 0, buffer.length)) != -1) {
                        zos.putNextEntry(new ZipEntry(entry));
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

                ClassReader reader = new ClassReader(bos.getBytes());

                if(refactorMap.isRemoveClass(reader.getClassName())) {
                    Log.info("Removed Entry \"" + entry.getName() + "\"");

                    bos.close();
                    bos = null;
                    continue;
                }

                ClassWriter writer = new ClassWriter(0);
                ClassRefactorer refactorer = new ClassRefactorer(writer, refactorMap);
                reader.accept(refactorer, 0);

                zos.putNextEntry(new ZipEntry(entry));
                zos.write(writer.toByteArray());

                Log.info("Refactored Entry \"" + entry.getName() + "\"");

                bos.close();
                bos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            JarUtil.close(fis, "FileInputStream");
            JarUtil.close(zis, "ZipInputStream");
            JarUtil.close(bos, "ByteOutputStream");
            JarUtil.close(fos, "FileOutputStream");
            JarUtil.close(zos, "ZipOutputStream");
        }
    }

    private static class ClassRefactorer extends ClassVisitor {

        private RefactorMap refactorMap;
        private ClassVisitor visitor;

        public ClassRefactorer(ClassVisitor visitor, RefactorMap refactorMap) {
            super(Opcodes.ASM4, visitor);

            this.visitor = visitor;
            this.refactorMap = refactorMap;
        }

    }

    private static class MethodRefactorer extends MethodVisitor {

        private RefactorMap refactorMap;
        private MethodVisitor visitor;

        public MethodRefactorer(MethodVisitor visitor, RefactorMap refactorMap) {
            super(Opcodes.ASM4, visitor);

            this.visitor = visitor;
            this.refactorMap = refactorMap;
        }

    }

    private static class FieldRefactorer extends FieldVisitor {

        private RefactorMap refactorMap;
        private FieldVisitor visitor;

        public FieldRefactorer(FieldVisitor visitor, RefactorMap refactorMap) {
            super(Opcodes.ASM4, visitor);

            this.visitor = visitor;
            this.refactorMap = refactorMap;
        }

    }

}
