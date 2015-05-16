package net.sothatsit.gamepackdownloader.pack.refactor;

import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.pack.io.ArchiveLoader;
import net.sothatsit.gamepackdownloader.pack.io.JarArchive;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;

public class ClassMap {

    private List<MapClass> classes;

    public ClassMap() {
        this.classes = new ArrayList<>();
    }

    public List<MapClass> getClasses() {
        return classes;
    }

    public MapClass getMapClass(String name) {
        for(MapClass clazz : classes) {
            if(clazz.getName().equals(name)) {
                return clazz;
            }
        }
        return null;
    }

    public void registerClass(int version, int access, String name, String signature, String superName, String[] interfaces) {
        MapClass existing = getMapClass(name);

        if(existing != null) {
            classes.remove(existing);
        }

        MapClass clazz = new MapClass(version, access, name, signature, superName, interfaces);

        classes.add(clazz);
    }

    public class MapClass {

        private int version;
        private int access;
        private String name;
        private String signature;
        private String superName;
        private String[] interfaces;
        private List<MapField> fields;
        private List<MapMethod> methods;

        public MapClass(int version, int access, String name, String signature, String superName, String[] interfaces) {
            this.version = version;
            this.access = access;
            this.name = name;
            this.signature = signature;
            this.superName = superName;
            this.interfaces = interfaces;

            this.fields = new ArrayList<>();
            this.methods = new ArrayList<>();
        }

        public int getVersion() {
            return version;
        }

        public int getAccess() {
            return access;
        }

        public String getName() {
            return name;
        }

        public String getSignature() {
            return signature;
        }

        public String getSuperName() {
            return superName;
        }

        public String[] getInterfaces() {
            return interfaces;
        }

        public List<MapField> getFields() {
            return fields;
        }

        public List<MapMethod> getMethods() {
            return methods;
        }

        public void registerField(int access, String name, String desc, String signature, Object value) {
            MapField existing = getField(name);

            if(existing != null) {
                fields.remove(existing);
            }

            this.fields.add(new MapField(access, name, desc, signature, value));
        }

        public void registerMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MapMethod existing = getMethod(name);

            if(existing != null) {
                methods.remove(existing);
            }

            this.methods.add(new MapMethod(access, name, desc, signature, exceptions));
        }

        public MapField getField(String name) {
            for(MapField field : fields) {
                if(field.getName().equals(name)) {
                    return field;
                }
            }
            return null;
        }

        public MapMethod getMethod(String name) {
            for(MapMethod method : methods) {
                if(method.getName().equals(name)) {
                    return method;
                }
            }
            return null;
        }

    }

    public class MapMethod {

        private int access;
        private String name;
        private String desc;
        private String signature;
        private String[] exceptions;

        public MapMethod(int access, String name, String desc, String signature, String[] exceptions) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        public int getAccess() {
            return access;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getSignature() {
            return signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }

    }

    public class MapField {

        private int access;
        private String name;
        private String desc;
        private String signature;
        private Object value;

        public MapField(int access, String name, String desc, String signature, Object value) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.value = value;
        }

        public int getAccess() {
            return access;
        }

        public String getName() {
            return name;
        }

        public String getDesc() {
            return desc;
        }

        public String getSignature() {
            return signature;
        }

        public Object getValue() {
            return value;
        }

    }

    public static ClassMap build(JarArchive archive) throws IOException {
        final ClassMap map = new ClassMap();

        final ClassVisitor visitor = new ClassVisitor(Opcodes.ASM4) {
            private int classVersion;
            private int classAccess;
            private String className;
            private String classSignature;
            private String classSuperName;
            private String[] classInterfaces;

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                this.classVersion = version;
                this.classAccess = access;
                this.className = name;
                this.classSignature = signature;
                this.classSuperName = superName;
                this.classInterfaces = interfaces;

                map.registerClass(version, access, name, signature, superName, interfaces);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                map.getMapClass(className).registerField(access, name, desc, signature, exceptions);
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                map.getMapClass(name).registerField(access, name, desc, signature, value);
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

        return map;
    }

}
