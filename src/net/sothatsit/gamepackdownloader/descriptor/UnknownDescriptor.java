package net.sothatsit.gamepackdownloader.descriptor;

import java.util.ArrayList;
import java.util.List;

public class UnknownDescriptor extends Descriptor {

    private String start;
    private String[] classes;
    private String[] text;

    public UnknownDescriptor(String descriptorRaw, ClassNameSupplier supplier) {
        super(descriptorRaw);

        StringBuilder start = new StringBuilder();
        List<String> classes = new ArrayList<>();
        List<String> text = new ArrayList<>();
        StringBuilder clazzBuilder = null;
        StringBuilder textBuilder = null;

        for(int i=0; i<descriptorRaw.length(); i++) {
            char c = descriptorRaw.charAt(i);
            if(clazzBuilder != null) {
                clazzBuilder.append(c);

                if(c == ';') {
                    classes.add(clazzBuilder.toString());
                    clazzBuilder = null;
                    textBuilder = new StringBuilder();
                    continue;
                }

                if(i >= descriptorRaw.length() - 1) {
                    text.add(clazzBuilder.toString());
                    clazzBuilder = null;
                }

                continue;
            }

            if(c == 'L') {
                if(start != null) {
                    this.start = start.toString();
                    start = null;
                }

                if(textBuilder != null) {
                    text.add(textBuilder.toString());
                    textBuilder = null;
                }

                clazzBuilder = new StringBuilder();
                clazzBuilder.append(c);
            }

            if(start != null) {
                start.append(c);

                if(i >= descriptorRaw.length() - 1) {
                    this.start = start.toString();
                    start = null;
                }
            }

            if(textBuilder != null) {
                textBuilder.append(c);

                if(i >= descriptorRaw.length() - 1) {
                    text.add(textBuilder.toString());
                    textBuilder = null;
                }
            }
        }

        this.classes = classes.toArray(new String[0]);
        this.text = text.toArray(new String[0]);
    }

    @Override
    public String getDescriptorReformatted() {
        return getWorkingDescriptor();
    }

    @Override
    public String getWorkingDescriptor() {
        StringBuilder builder = new StringBuilder();

        builder.append(start);

        for(int i=0; i < classes.length; i++) {
            builder.append(classes[i]);

            if(i < text.length) {
                builder.append(text[i]);
            }
        }

        return builder.toString();
    }

    public String getStart() {
        return start;
    }

    public String[] getClasses() {
        return classes;
    }

    public String[] getText() {
        return text;
    }

    public static String getClassName(String fullName) {
        int index = fullName.lastIndexOf('/');

        if (index >= 0) {
            return fullName.substring(index + 1);
        }

        return fullName;
    }

}
