package net.sothatsit.gamepackdownloader.pack.refactor.descriptor;

import java.util.HashMap;
import java.util.Map;

public abstract class Descriptor {

    private String descriptorRaw;

    public Descriptor(String descriptorRaw) {
        this.descriptorRaw = descriptorRaw;
    }

    public String getDescriptorRaw() {
        return this.descriptorRaw;
    }

    public abstract String getDescriptorReformatted();

    public static final Map<Character, String> fullNames = new HashMap<>();

    static {
        fullNames.put('B', "java/lang/Byte");
        fullNames.put('C', "java/lang/Char");
        fullNames.put('D', "java/lang/Double");
        fullNames.put('F', "java/lang/Float");
        fullNames.put('I', "java/lang/Integer");
        fullNames.put('J', "java/lang/Long");
        fullNames.put('S', "java/lang/Short");
        fullNames.put('Z', "java/lang/Boolean");
        fullNames.put('V', "java/lang/Void");
        fullNames.put('G', "Group2Empty");
        fullNames.put('N', "NotInitialized");
        fullNames.put('A', "Address");
        fullNames.put('X', "ByteChar");
        fullNames.put('Y', "java/lang/Character");
        fullNames.put('U', "Unknown");
    }

    public static String getFullName(String descriptorPart, ClassNameSupplier supplier) {
        if (descriptorPart.length() == 0) {
            return "";
        }

        StringBuilder append = new StringBuilder();
        while (descriptorPart.length() > 1 && descriptorPart.charAt(0) == '[') {
            append.append("Array");
            descriptorPart = descriptorPart.substring(1);
        }

        if (descriptorPart.charAt(0) == 'L') {
            if (descriptorPart.charAt(descriptorPart.length() - 1) != ';') {
                return descriptorPart;
            }

            String baseName = descriptorPart.substring(1, descriptorPart.length() - 1);
            String clazz = (supplier == null ? baseName : supplier.getClassName(baseName));

            return clazz + append;
        }

        if (fullNames.containsKey(descriptorPart.charAt(0))) {
            return fullNames.get(descriptorPart.charAt(0)) + append;
        }

        return descriptorPart + append;
    }

    public static String getShortName(String fullName) {
        int index = fullName.lastIndexOf('/');

        if(index < 0 || index >= fullName.length() - 1) {
            return fullName;
        }

        return fullName.substring(index + 1);
    }
}
