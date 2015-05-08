package net.sothatsit.gamepackdownloader;

import org.jetbrains.java.decompiler.modules.renamer.ConverterHelper;

public class GamePackRenamer extends ConverterHelper {

    @Override
    public boolean toBeRenamed(Type elementType, String className, String element, String descriptor) {
        return super.toBeRenamed(elementType, className, element, descriptor);
    }

    @Override
    public String getNextClassName(String fullName, String shortName) {
        return super.getNextClassName(fullName, shortName);
    }

    @Override
    public String getNextFieldName(String className, String field, String descriptor) {
        return super.getNextFieldName(className, field, descriptor);
    }

    @Override
    public String getNextMethodName(String className, String method, String descriptor) {
        return super.getNextMethodName(className, method, descriptor);
    }

}
