package net.sothatsit.gamepackdownloader.refactor;

import java.util.ArrayList;
import java.util.List;

public class MethodDescriptor extends Descriptor {

    private String[] arguments;
    private String returnType;

    public MethodDescriptor(String descriptorRaw, ClassNameStore store) {
        super(descriptorRaw);

        String[] split = BasicYAML.splitAtFirst(descriptorRaw, ")");

        String args;
        String ret;

        if(split[1].isEmpty()) {
            args = "";
            ret = split[0];
        } else if(split[0].length() > 1){
            args = split[0].substring(1);
            ret = split[1];
        } else {
            args = "";
            ret = split[1];
        }

        this.returnType = Descriptor.getFullName(ret, store);

        List<String> arguments = new ArrayList<>();

        StringBuilder builder = null;
        StringBuilder append = null;
        int i=0;
        while(i < args.length()) {
            char c = args.charAt(i);

            String add = (append == null ? "" : append.toString());

            if(builder != null) {
                builder.append(c);

                if(c == ';') {
                    arguments.add(Descriptor.getFullName(add + builder.toString(), store));
                    builder = null;
                    append = null;
                }
            } else {
                if(c == '[') {
                    if(append == null) {
                        append = new StringBuilder();
                    }

                    append.append('[');
                } else if(c == 'L') {
                    builder = new StringBuilder();
                    builder.append(c);
                } else {
                    arguments.add(Descriptor.getFullName(add + Character.toString(c), store));
                    append = null;
                }
            }

            i++;
        }

        this.arguments = arguments.toArray(new String[0]);
    }

    @Override
    public String getDescriptorReformatted() {
        StringBuilder builder = new StringBuilder("a");

        for(String str : arguments) {
            builder.append(str);
            builder.append('_');
        }

        builder.append("r");
        builder.append(returnType);

        return builder.toString();
    }

    public String getReturnType() {
        return returnType;
    }

    public String[] getArguments() {
        return arguments;
    }

}