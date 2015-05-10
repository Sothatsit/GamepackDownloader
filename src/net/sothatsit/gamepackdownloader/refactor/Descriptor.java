package net.sothatsit.gamepackdownloader.refactor;

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
        fullNames.put('B', "Byte");
        fullNames.put('C', "Char");
        fullNames.put('D', "Double");
        fullNames.put('F', "Float");
        fullNames.put('I', "Int");
        fullNames.put('J', "Long");
        fullNames.put('S', "Short");
        fullNames.put('Z', "Boolean");
        fullNames.put('V', "Void");
        fullNames.put('G', "Group2Empty");
        fullNames.put('N', "NotInitialized");
        fullNames.put('A', "Address");
        fullNames.put('X', "ByteChar");
        fullNames.put('Y', "ShortChar");
        fullNames.put('U', "Unknown");
    }

    public String getFullName(String descriptorPart, ClassNameStore store) {
        if(descriptorPart.length() == 0) {
            return "";
        }

        StringBuilder append = new StringBuilder();
        while(descriptorPart.length() > 1 && descriptorPart.charAt(0) == '[') {
            append.append("Array");
            descriptorPart = descriptorPart.substring(1);
        }

        if(descriptorPart.charAt(0) == 'L') {
            if(descriptorPart.charAt(descriptorPart.length() - 1) != ';') {
                return descriptorPart;
            }

            String clazz = store.getClassName(descriptorPart.substring(1, descriptorPart.length() - 2));

            return clazz + append;
        }

        if(fullNames.containsKey(descriptorPart.charAt(0))) {
            return fullNames.get(descriptorPart.charAt(0)) + append;
        }

        return descriptorPart + append;
    }
}
