package net.sothatsit.gamepackdownloader;

import net.sothatsit.gamepackdownloader.refactor.BasicYAML;
import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper;

public class GamePackRenamer extends ConverterHelper {

    @Override
    public boolean toBeRenamed(Type elementType, String className, String element, String descriptor) {
        return super.toBeRenamed(elementType, className, element, descriptor);
    }

    @Override
    public String getNextClassName(String fullName, String shortName) {
        String name = super.getNextClassName(fullName, shortName);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + name + ".txt");

        GamePackDownloader.info("Loading " + name);

        if(yaml != null && yaml.isSet("class-name")) {
            name = yaml.getValue("class-name");
        }

        return name;
    }

    @Override
    public String getNextFieldName(String className, String field, String descriptor) {
        String name = super.getNextFieldName(className, field, descriptor);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + className + ".txt");

        if(yaml != null && yaml.isSet(name)) {
            name = yaml.getValue(name);
        }

        return name;
    }

    @Override
    public String getNextMethodName(String className, String method, String descriptor) {
        String name = super.getNextMethodName(className, method, descriptor);

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + className + ".txt");

        if(yaml != null && yaml.isSet(name)) {
            name = yaml.getValue(name);
        }

        return name;
    }

}
