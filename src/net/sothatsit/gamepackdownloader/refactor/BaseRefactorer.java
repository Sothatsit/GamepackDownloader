package net.sothatsit.gamepackdownloader.refactor;

import net.sothatsit.gamepackdownloader.descriptor.ClassNameSupplier;
import net.sothatsit.gamepackdownloader.descriptor.Descriptor;
import net.sothatsit.gamepackdownloader.descriptor.FieldDescriptor;
import net.sothatsit.gamepackdownloader.descriptor.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;

public class BaseRefactorer implements IClassRenamer, IFieldRenamer, IMethodRenamer {

    private final ClassNameSupplier supplier = (this instanceof ClassNameSupplier ? (ClassNameSupplier) this : null);
    private List<String> classNames = new ArrayList<>();
    private List<String> fieldNames = new ArrayList<>();
    private List<String> methodNames = new ArrayList<>();

    @Override
    public String getNewName(int version, int access, String name, String signature, String superName, String[] interfaces) {
        StringBuilder newName = new StringBuilder("class");

        if(superName != null && !Descriptor.getShortName(superName).equals("Object")) {
            newName.append("_e");
            newName.append(Descriptor.getShortName(Descriptor.getFullName(superName, supplier)));
        }

        for(String str : interfaces) {
            newName.append("_i");
            newName.append(Descriptor.getShortName(Descriptor.getFullName(str, supplier)));
        }

        String str = new String(newName);

        int index = 1;
        while(str.equals("class") || classNames.contains(str)) {
            str = newName.toString() + "_" + index;
            index++;
        }

        classNames.add(str);

        return str;
    }

    @Override
    public String getNewName(String className, int access, String name, String desc, String signature, Object value) {
        FieldDescriptor descriptor = new FieldDescriptor(desc, supplier);

        final String nameBase = "f_" + Descriptor.getShortName(descriptor.getDescriptorReformatted());

        String newName = nameBase;

        int index = 1;
        while(fieldNames.contains(newName)) {
            newName = nameBase + "_" + index;
            index++;
        }

        fieldNames.add(className + "_" + newName);

        return newName;
    }

    @Override
    public String getNewName(String className, int access, String name, String desc, String signature, String[] exceptions) {
        MethodDescriptor descriptor = new MethodDescriptor(desc, supplier);

        final String nameBase = "m_" + Descriptor.getShortName(descriptor.getDescriptorReformatted());

        String newName = nameBase;

        int index = 1;
        while(methodNames.contains(newName)) {
            newName = nameBase + "_" + index;
            index++;
        }

        methodNames.add(className + "_" + newName);

        return newName;
    }

}
