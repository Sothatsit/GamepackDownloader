package net.sothatsit.gamepackdownloader.pack.refactor;

import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.pack.io.ArchiveLoader;
import net.sothatsit.gamepackdownloader.pack.io.JarArchive;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public class RefactorMapBuilder {

    private IClassRenamer classRenamer;
    private IFieldRenamer fieldRenamer;
    private IMethodRenamer methodRenamer;

    protected RefactorMapBuilder(IClassRenamer classRenamer, IFieldRenamer fieldRenamer, IMethodRenamer methodRenamer) {
        this.classRenamer = classRenamer;
        this.fieldRenamer = fieldRenamer;
        this.methodRenamer = methodRenamer;
    }

    public RefactorMap build(JarArchive archive) throws IOException {
        final RefactorMap map = new RefactorMap();

        final ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4) {
            private String clazz;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                this.clazz = name;

                if(classRenamer != null) {
                    map.setClassName(name, classRenamer.getNewName(name, interfaces, superName));
                }
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if(fieldRenamer != null) {
                    map.setFieldName(clazz, name, fieldRenamer.getNewName(clazz, name, desc));
                }

                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                if(methodRenamer != null) {
                    map.setMethodName(clazz, name, methodRenamer.getNewName(clazz, name, desc));
                }

                return super.visitField(access, name, desc, signature, value);
            }

        };

        archive.loadArchive(new ArchiveLoader() {
            @Override
            public boolean shouldLoad(File file, ZipEntry entry) throws IOException {
                return entry.getName().endsWith(".class");
            }

            @Override
            public void onLoad(File file, ZipEntry entry, byte[] data) throws IOException {
                new ClassReader(data).accept(visitor, 0);
            }
        });

        map.fixDuplicates();

        return map;
    }

}
