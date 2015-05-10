package net.sothatsit.gamepackdownloader.refactor;

import java.util.HashMap;
import java.util.Map;

public class ClassNameStore {

    private Map<String, String> classNames;
    private Map<String, String> yamlFiles;

    public  ClassNameStore() {
        classNames = new HashMap<>();
        yamlFiles = new HashMap<>();
    }

    public void registerClass(String oldName, String newName, String yamlFile) {
        classNames.put(oldName, newName);
        yamlFiles.put(newName, yamlFile);
    }

    public String getClassName(String oldName) {
        return (classNames.containsKey(oldName) ? classNames.get(oldName) : oldName);
    }

    public String getYamlFile(String newName) {
        return (yamlFiles.containsKey(newName) ? yamlFiles.get(newName) : newName);
    }

}
