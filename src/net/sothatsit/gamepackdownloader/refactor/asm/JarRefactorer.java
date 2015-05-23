package net.sothatsit.gamepackdownloader.refactor.asm;

import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.ClassWriter;
import net.sothatsit.gamepackdownloader.GamePackDownloader;
import net.sothatsit.gamepackdownloader.io.ArchiveEdit;
import net.sothatsit.gamepackdownloader.io.ArchiveEditor;
import net.sothatsit.gamepackdownloader.io.JarArchive;
import net.sothatsit.gamepackdownloader.refactor.IClassRenamer;
import net.sothatsit.gamepackdownloader.refactor.IFieldRenamer;
import net.sothatsit.gamepackdownloader.refactor.IMethodRenamer;
import net.sothatsit.gamepackdownloader.rename.RefactorMap;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class JarRefactorer {

    public static void refactor(JarArchive archive, IClassRenamer classRenamer, IFieldRenamer fieldRenamer, IMethodRenamer methodRenamer) throws IOException {
        RefactorMap refactorMap = RefactorMap.builder(classRenamer, fieldRenamer, methodRenamer).build(archive);

        refactor(archive, refactorMap);
    }

    public static void refactor(final JarArchive archive, final RefactorMap refactorMap) throws IOException {
        GamePackDownloader.info("Refactoring Jar File \"" + archive.getJarFile().getAbsolutePath() + "\"");
        archive.editArchive(new ArchiveEditor() {
            @Override
            public boolean shouldEdit(File file, ZipEntry entry) throws IOException {
                return entry.getName().endsWith(".class");
            }

            @Override
            public ArchiveEdit edit(File file, ZipEntry entry, byte[] data) throws IOException {
                GamePackDownloader.info("Refactoring \"" + entry.getName() + "\"");
                return refactor(data, refactorMap);
            }
        });
    }

    public static ArchiveEdit refactor(byte[] bytecode, RefactorMap refactorMap) {
        ClassReader reader = new ClassReader(bytecode);

        String className = refactorMap.getNewClassName(reader.getClassName());

        ClassWriter writer = new ClassWriter(reader, 0);
        ClassRefactorer refactorer = new ClassRefactorer(writer, refactorMap);
        reader.accept(refactorer, 0);

        return new ArchiveEdit(className + ".class", writer.toByteArray());
    }

}
