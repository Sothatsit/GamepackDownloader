package net.sothatsit.gamepackdownloader.bytecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenameMap {

    private List<RenameClass> classes;

    public RenameMap() {
        this.classes = new ArrayList<>();
    }

    private RenameClass getRenameClass(String oldName) {
        for(RenameClass clazz : classes) {
            if(clazz.getOldName().equals(oldName)) {
                return clazz;
            }
        }

        RenameClass clazz = new RenameClass(oldName);

        classes.add(clazz);

        return clazz;
    }

    public String getNewClassName(String oldName) {
        return getRenameClass(oldName).getNewName();
    }

    public String getOldClassName(String newName) {
        for(RenameClass clazz : classes) {
            if(clazz.getNewName().equals(newName)) {
                return clazz.getOldName();
            }
        }
        return newName;
    }

    public String getNewFieldName(String clazz, String oldName) {
        return getRenameClass(clazz).getFieldName(oldName);
    }

    public String getNewMethodName(String clazz, String oldName) {
        return getRenameClass(clazz).getMethodName(oldName);
    }

    public String getOldFieldName(String clazz, String newName) {
        return getRenameClass(clazz).getOldFieldName(newName);
    }

    public String getOldMethodName(String clazz, String newName) {
        return getRenameClass(clazz).getOldMethodName(newName);
    }

    public void setClassName(String oldName, String newName) {
        getRenameClass(oldName).setClassName(newName);
    }

    public void setFieldName(String clazz, String oldName, String newName) {
        getRenameClass(clazz).setFieldName(oldName, newName);
    }

    public void setMethodName(String clazz, String oldName, String newName) {
        getRenameClass(clazz).setMethodName(oldName, newName);
    }

    public void fixDuplicates() {
        fixDuplicateClassNames();

        for(RenameClass clazz : classes) {
            clazz.fixDuplicateFieldNames();
            clazz.fixDuplicateMethodNames();
        }
    }

    public void fixDuplicateClassNames() {
        List<String> names = new ArrayList<>();

        for(RenameClass clazz : classes) {
            String name = clazz.getNewName();
            int index = 0;
            while(names.contains(clazz.getNewName())) {
                name = clazz.getNewName() + "_" + index++;
            }

            if(index > 0) {
                clazz.setClassName(name);
            }

            names.add(name);
        }
    }

    private class RenameClass {
        private String oldName;
        private String newName;
        private Map<String, String> fieldNames;
        private Map<String, String> methodNames;

        public RenameClass(String name) {
            this.oldName = name;
            this.newName = null;
            this.fieldNames = new HashMap<>();
            this.methodNames = new HashMap<>();
        }

        public String getOldName() {
            return oldName;
        }

        public String getNewName() {
            return newName;
        }

        public String getFieldName(String oldName) {
            return fieldNames.getOrDefault(oldName, oldName);
        }

        public String getMethodName(String oldName) {
            return methodNames.getOrDefault(oldName, oldName);
        }

        public String getOldFieldName(String newName) {
            for(Map.Entry<String, String> entry : fieldNames.entrySet()) {
                if(entry.getValue().equals(newName)) {
                    return entry.getKey();
                }
            }
            return newName;
        }

        public String getOldMethodName(String newName) {
            for(Map.Entry<String, String> entry : methodNames.entrySet()) {
                if(entry.getValue().equals(newName)) {
                    return entry.getKey();
                }
            }
            return newName;
        }

        public void setClassName(String className) {
            this.newName = className;
        }

        public void setFieldName(String oldName, String newName) {
            this.fieldNames.put(oldName, newName);
        }

        public void setMethodName(String oldName, String newName) {
            this.methodNames.put(oldName, newName);
        }

        public void fixDuplicateFieldNames() {
            List<String> names = new ArrayList<>();

            Map.Entry<String, String>[] entries = fieldNames.entrySet().toArray(new Map.Entry[0]);
            for(int i=0; i < entries.length; i++) {
                Map.Entry<String, String> entry = entries[i];

                String name = entry.getValue();
                int index = 0;
                while(names.contains(entry.getValue())) {
                    name = entry.getValue() + "_" + index++;
                }

                if(index > 0) {
                    setFieldName(entry.getKey(), name);
                    entries = fieldNames.entrySet().toArray(new Map.Entry[0]);
                }

                names.add(name);
            }
        }

        public void fixDuplicateMethodNames() {
            List<String> names = new ArrayList<>();

            Map.Entry<String, String>[] entries = methodNames.entrySet().toArray(new Map.Entry[0]);
            for(int i=0; i < entries.length; i++) {
                Map.Entry<String, String> entry = entries[i];

                String name = entry.getValue();
                int index = 0;
                while(names.contains(entry.getValue())) {
                    name = entry.getValue() + "_" + index++;
                }

                if(index > 0) {
                    setMethodName(entry.getKey(), name);
                    entries = methodNames.entrySet().toArray(new Map.Entry[0]);
                }

                names.add(name);
            }
        }
    }

}
