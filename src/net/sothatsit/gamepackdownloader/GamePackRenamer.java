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
        final String initName = super.getNextClassName(fullName, shortName);
        String name = initName;

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + name + ".txt");

        if(yaml != null && yaml.isSet("class-name")) {
            name = yaml.getValue("class-name");
            GamePackDownloader.info("Renamed class \"" + initName + "\" to \'" + name + '"');
        }

        return name;
    }

    @Override
    public String getNextFieldName(String className, String field, String descriptor) {
        final String initName = super.getNextFieldName(className, field, descriptor);
        String name = initName;

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + className + ".txt");

        if(yaml != null && yaml.isSet(name)) {
            name = yaml.getValue(name);
            GamePackDownloader.info("Renamed field \"" + initName + "\" of class \"" + className + "\" to '" + name + '"');
        }

        return name;
    }

    @Override
    public String getNextMethodName(String className, String method, String descriptor) {
        final String initName = super.getNextMethodName(className, method, descriptor);
        String name = initName;

        BasicYAML yaml = BasicYAML.getFile("/refactorings/" + className + ".txt");

        if(yaml != null && yaml.isSet(name)) {
            name = yaml.getValue(name);
            GamePackDownloader.info("Renamed method \"" + initName + "\" of class \"\" + className + \"\" to '" + name + '"');
        }

        return name;
    }

}
