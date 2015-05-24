package net.sothatsit.gamepackdownloader.descriptor;

import java.util.ArrayList;
import java.util.List;

public class MethodDescriptor extends Descriptor {

    private String[] arguments;
    private String returnType;

    public MethodDescriptor(String descriptorRaw, ClassNameSupplier supplier) {
        super(descriptorRaw);

        if(descriptorRaw == null) {
            return;
        }

        String[] split = splitAtFirst(descriptorRaw, ')');

        String args;
        String ret;

        if (split[1].isEmpty()) {
            args = "";
            ret = split[0];
        } else if (split[0].length() > 1) {
            args = split[0].substring(1);
            ret = split[1];
        } else {
            args = "";
            ret = split[1];
        }

        this.returnType = Descriptor.getFullName(ret, supplier);

        List<String> arguments = new ArrayList<>();

        StringBuilder builder = null;
        StringBuilder append = null;
        int i = 0;
        while (i < args.length()) {
            char c = args.charAt(i);

            String add = (append == null ? "" : append.toString());

            if (builder != null) {
                builder.append(c);

                if (c == ';') {
                    arguments.add(Descriptor.getFullName(add + builder.toString(), supplier));
                    builder = null;
                    append = null;
                }
            } else {
                if (c == '[') {
                    if (append == null) {
                        append = new StringBuilder();
                    }

                    append.append('[');
                } else if (c == 'L') {
                    builder = new StringBuilder();
                    builder.append(c);
                } else {
                    arguments.add(Descriptor.getFullName(add + Character.toString(c), supplier));
                    append = null;
                }
            }

            i++;
        }

        this.arguments = arguments.toArray(new String[0]);
    }

    @Override
    public String getDescriptorReformatted() {
        if(getDescriptorRaw() == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder("a");

        for (String str : arguments) {
            builder.append(getClassName(str));
            builder.append('_');
        }

        builder.append("r");
        builder.append(returnType);

        return builder.toString();
    }

    @Override
    public String getWorkingDescriptor() {
        if(getDescriptorRaw() == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        builder.append('(');

        for(String argument : arguments) {
            builder.append(Descriptor.getDescriptorName(argument));
        }

        builder.append(')');

        builder.append(Descriptor.getDescriptorName(returnType));

        return builder.toString();
    }

    public String getReturnType() {
        return returnType;
    }

    public String[] getArguments() {
        return arguments;
    }

    public static String getClassName(String fullName) {
        int index = fullName.lastIndexOf('/');

        if (index >= 0) {
            return fullName.substring(index + 1);
        }

        return fullName;
    }

    public static String[] splitAtFirst(String str, char c) {
        if(str == null) {
            return new String[] {"", ""};
        }

        int index = str.indexOf(c);

        if(index == -1) {
            return new String[] {str, ""};
        }

        if(index == 0) {
            return new String[] {"", str.substring(1)};
        }

        if(index == str.length() - 1) {
            return new String[] {str.substring(0, str.length() - 1), ""};
        }

        return new String[] {str.substring(0, index - 1), str.substring(index + 1)};
    }

}
