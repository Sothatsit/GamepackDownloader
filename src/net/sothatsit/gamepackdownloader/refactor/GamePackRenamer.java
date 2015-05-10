package net.sothatsit.gamepackdownloader.refactor;

import net.sothatsit.gamepackdownloader.GamePackDownloader;
import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper;

import java.util.ArrayList;
import java.util.List;

public class GamePackRenamer extends ConverterHelper {

    private List<String> takenNames = new ArrayList<>();
    private ClassNameStore store = new ClassNameStore();

    @Override
    public boolean toBeRenamed(Type elementType, String className, String element, String descriptor) {
        return super.toBeRenamed(elementType, className, element, descriptor);
    }

    @Override
    public String getNextClassName(String fullName, String shortName) {
        final String initName = super.getNextClassName(fullName, shortName);
        String name = initName;

        GamePackDownloader.info("Class \"" + shortName + "\" to \"" + initName + "\", full-name: " + fullName);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + name + ".txt");

        if(yaml != null && yaml.isSet("class-name")) {
            name = yaml.getValue("class-name");
            GamePackDownloader.info("Renamed class \"" + initName + "\" to \"" + name + '"');
        }

        store.registerClass(fullName, name, initName);

        return name;
    }

    @Override
    public String getNextFieldName(String className, String field, String descriptorStr) {
        className = store.getClassName(className);

        FieldDescriptor descriptor = new FieldDescriptor(descriptorStr, store);

        final String initName = uniqueName("field", className, descriptor.getType());
        String name = initName;

        GamePackDownloader.info("Field \"" + field + "\" to \"" + initName + "\", descriptor: " + descriptorStr);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + store.getYamlFile(className) + ".txt");

        if(yaml != null && yaml.isSet("field_" + name)) {
            name = yaml.getValue("field_" + name);
            GamePackDownloader.info("Renamed field \"" + initName + "\" of class \"" + className + "\" to \"" + name + '"');
        }

        return name;
    }

    @Override
    public String getNextMethodName(String className, String method, String descriptor) {
        className = store.getClassName(className);

        final String initName = uniqueName("method", className, descriptor);
        String name = initName;

        GamePackDownloader.info("Method \"" + method + "\" to \"" + initName + "\", descriptor: " + descriptor);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + store.getYamlFile(className) + ".txt");

        if(yaml != null && yaml.isSet("method_" + name)) {
            name = yaml.getValue("method_" + name);
            GamePackDownloader.info("Renamed method \"" + initName + "\" of class \"\" + className + \"\" to \"" + name + '"');
        }

        return name;
    }

    public String uniqueName(String prefix, String clazz, String descriptor) {
        String initName = prefix + '_' + clazz + '_' + descriptor;
        String name;

        int index = 0;
        do {
            index++;
            name = initName + "_" + index;
        } while(takenNames.contains(name));

        takenNames.add(name);

        return validate(name);
    }

    public String validate(String name) {
        StringBuilder builder = new StringBuilder();

        for(int i=0; i<name.length(); i++) {
            char c = name.charAt(i);

            if(i == 0 && !Character.isJavaIdentifierStart(c)) {
                builder.append('m');
            } else if(!Character.isJavaIdentifierPart(c)) {
                builder.append('_');
            }else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
