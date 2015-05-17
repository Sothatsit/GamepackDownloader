package net.sothatsit.gamepackdownloader.pack.refactor.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import net.sothatsit.gamepackdownloader.pack.io.ArchiveEditor;
import net.sothatsit.gamepackdownloader.pack.io.JarArchive;
import net.sothatsit.gamepackdownloader.pack.refactor.IClassRenamer;
import net.sothatsit.gamepackdownloader.pack.refactor.IFieldRenamer;
import net.sothatsit.gamepackdownloader.pack.refactor.IMethodRenamer;
import net.sothatsit.gamepackdownloader.pack.refactor.RefactorMap;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class JarRefactorer {

    public static void refactor(JarArchive archive, IClassRenamer classRenamer, IFieldRenamer fieldRenamer, IMethodRenamer methodRenamer) throws IOException {
        RefactorMap refactorMap = RefactorMap.builder(classRenamer, fieldRenamer, methodRenamer).build(archive);

        refactor(archive, refactorMap);
    }

    public static void refactor(final JarArchive archive, final RefactorMap refactorMap) throws IOException {
        archive.editArchive(new ArchiveEditor() {
            @Override
            public boolean shouldEdit(File file, ZipEntry entry) throws IOException {
                return entry.getName().endsWith(".class");
            }

            @Override
            public byte[] edit(File file, ZipEntry entry, byte[] data) throws IOException {
                return refactor(data, refactorMap);
            }
        });
    }

    public static byte[] refactor(byte[] bytecode, RefactorMap refactorMap) {
        ClassReader reader = new ClassReader(bytecode);
        ClassWriter writer = new ClassWriter(reader, 0);
        ClassRefactorer refactorer = new ClassRefactorer(writer, refactorMap);
        reader.accept(refactorer, 0);
        return writer.toByteArray();
    }

}
