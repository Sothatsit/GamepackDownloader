package net.sothatsit.gamepackdownloader.refactor;

import jdk.internal.org.objectweb.asm.*;
import net.sothatsit.gamepackdownloader.io.ArchiveLoader;
import net.sothatsit.gamepackdownloader.io.JarArchive;
import net.sothatsit.gamepackdownloader.refactor.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.refactor.descriptor.MethodDescriptor;

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

            this.fields.add(new MapField(this, access, name, desc, signature, value));
        }

        public void registerMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MapMethod existing = getMethod(name);

            if(existing != null) {
                methods.remove(existing);
            }

            this.methods.add(new MapMethod(this, access, name, desc, signature, exceptions));
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

        public MapClass getSuperClass() {
            return getMapClass(superName);
        }

        public MapClass[] getInterfaceClasses() {
            MapClass[] classes = new MapClass[interfaces.length];

            for(int i=0; i<interfaces.length; i++) {
                classes[i] = getMapClass(interfaces[i]);
            }

            return classes;
        }

        public boolean isImplementing(String clazz) {
            return name.equals(clazz) || contains(interfaces, clazz);
        }

        public List<MapClass> getImplementingClasses() {
            List<MapClass> classes = new ArrayList<>();
            for(MapClass clazz : classes) {
                if(clazz.isImplementing(name)) {
                    classes.add(clazz);
                }
            }
            return classes;
        }

    }

    public class MapMethod {

        private MapClass clazz;
        private int access;
        private String name;
        private String desc;
        private String signature;
        private String[] exceptions;

        public MapMethod(MapClass clazz, int access, String name, String desc, String signature, String[] exceptions) {
            this.clazz = clazz;
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.exceptions = exceptions;
        }

        public MapClass getClazz() {
            return clazz;
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

        public String getReturnType() {
            MethodDescriptor descriptor = new MethodDescriptor(desc, null);

            return descriptor.getReturnType();
        }

        public MapClass getReturnClass() {
            MethodDescriptor descriptor = new MethodDescriptor(desc, null);

            return getMapClass(descriptor.getReturnType());
        }

        public String[] getArguments() {
            MethodDescriptor descriptor = new MethodDescriptor(desc, null);

            return descriptor.getArguments();
        }

        public MapClass[] getArgumentClasses() {
            MethodDescriptor descriptor = new MethodDescriptor(desc, null);

            MapClass[] classes = new MapClass[descriptor.getArguments().length];

            for(int i=0; i < descriptor.getArguments().length; i++) {
                classes[i] = getMapClass(descriptor.getArguments()[i]);
            }

            return classes;
        }

        public String getSignature() {
            return signature;
        }

        public String[] getExceptions() {
            return exceptions;
        }

    }

    public class MapField {

        private MapClass clazz;
        private int access;
        private String name;
        private String desc;
        private String signature;
        private Object value;

        public MapField(MapClass clazz, int access, String name, String desc, String signature, Object value) {
            this.access = access;
            this.name = name;
            this.desc = desc;
            this.signature = signature;
            this.value = value;
        }

        public MapClass getClazz() {
            return clazz;
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


        public String getType() {
            FieldDescriptor descriptor = new FieldDescriptor(desc, null);

            return descriptor.getType();
        }
        public MapClass getTypeClass() {
            FieldDescriptor descriptor = new FieldDescriptor(desc, null);

            return getMapClass(descriptor.getType());
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
                map.getMapClass(className).registerMethod(access, name, desc, signature, exceptions);
                return super.visitMethod(access, name, desc, signature, exceptions);
            }

            @Override
            public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
                map.getMapClass(className).registerField(access, name, desc, signature, value);
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

    private static boolean contains(String[] array, String str) {
        if(str == null || array.length == 0) {
            return false;
        }

        for(String s : array) {
            if(s != null && s.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

}
