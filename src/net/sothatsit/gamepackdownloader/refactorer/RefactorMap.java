package net.sothatsit.gamepackdownloader.refactorer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RefactorMap {

    private List<RenameClass> classes;
    private List<String> removeClasses;

    public RefactorMap() {
        this.classes = new ArrayList<>();
    }

    public List<RenameClass> getRenameClasses() {
        return classes;
    }

    public RenameClass createRenameClass(String oldName) {
        for(RenameClass clazz : classes) {
            if(clazz.getOldName().equals(oldName)) {
                return clazz;
            }
        }

        RenameClass clazz = new RenameClass(oldName);

        classes.add(clazz);

        return clazz;
    }

    public RenameClass getRenameClass(String oldName) {
        for(RenameClass clazz : classes) {
            if(clazz.getOldName().equals(oldName)) {
                return clazz;
            }
        }
        return null;
    }

    public String getNewClassName(String oldName) {
        RenameClass renameClass = getRenameClass(oldName);

        return renameClass == null ? oldName : renameClass.getNewName();
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
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass == null ? oldName : renameClass.getFieldName(oldName);
    }

    public String getNewMethodName(String clazz, String oldName) {
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass == null ? oldName : renameClass.getMethodName(oldName);
    }

    public String getOldFieldName(String clazz, String newName) {
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass == null ? newName : renameClass.getOldFieldName(newName);
    }

    public String getOldMethodName(String clazz, String newName) {
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass == null ? newName : renameClass.getOldMethodName(newName);
    }

    public void setClassName(String oldName, String newName) {
        createRenameClass(oldName).setClassName(newName);
    }

    public void setFieldName(String clazz, String oldName, String newName) {
        createRenameClass(clazz).setFieldName(oldName, newName);
    }

    public void setMethodName(String clazz, String oldName, String newName) {
        createRenameClass(clazz).setMethodName(oldName, newName);
    }

    public boolean isRemoveClass(String name) {
        return this.removeClasses.contains(name);
    }

    public void setRemoveClass(String name, boolean remove) {
        if(isRemoveClass(name) == remove) {
            return;
        }

        if(remove) {
            this.removeClasses.add(name);
        } else {
            this.removeClasses.remove(name);
        }
    }

    public boolean isRemoveField(String clazz, String fieldName, String fieldDesc) {
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass != null && renameClass.isRemoveField(fieldName, fieldDesc);
    }

    public boolean isRemoveMethod(String clazz, String methodName, String methodDesc) {
        RenameClass renameClass = getRenameClass(clazz);
        return renameClass != null && renameClass.isRemoveMethod(methodName, methodDesc);
    }

    public void setRemoveField(String clazz, String fieldName, String fieldDesc, boolean remove) {
        createRenameClass(clazz).setRemoveField(fieldName, fieldDesc, remove);
    }

    public void setRemoveMethod(String clazz, String methodName, String methodDesc, boolean remove) {
        createRenameClass(clazz).setRemoveMethod(methodName, methodDesc, remove);
    }

    public class RenameClass {
        private String oldName;
        private String newName;
        private Map<String, String> fieldNames;
        private Map<String, String> methodNames;
        private List<String> removeMethods;
        private List<String> removeFields;

        public RenameClass(String name) {
            this.oldName = name;
            this.newName = null;
            this.fieldNames = new HashMap<>();
            this.methodNames = new HashMap<>();
        }

        public Map<String, String> getFieldNames() {
            return fieldNames;
        }

        public Map<String, String> getMethodNames() {
            return methodNames;
        }

        public List<String> getRemoveMethods() {
            return removeMethods;
        }

        public List<String> getRemoveFields() {
            return removeFields;
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

        public boolean isRemoveMethod(String methodName, String methodDesc) {
            return this.removeMethods.contains(combine(methodName, methodDesc));
        }

        public boolean isRemoveField(String fieldName, String fieldDesc) {
            return this.removeFields.contains(combine(fieldName, fieldDesc));
        }

        public void setRemoveMethod(String methodName, String methodDesc, boolean remove) {
            if(remove == isRemoveMethod(methodName, methodDesc)) {
                return;
            }

            if(remove) {
                this.removeMethods.add(combine(methodName, methodDesc));
            } else {
                this.removeMethods.remove(combine(methodName, methodDesc));
            }
        }

        public void setRemoveField(String fieldName, String fieldDesc, boolean remove) {
            if(remove == isRemoveField(fieldName, fieldDesc)) {
                return;
            }

            if(remove) {
                this.removeFields.add(combine(fieldName, fieldDesc));
            } else {
                this.removeFields.remove(combine(fieldName, fieldDesc));
            }
        }
    }

    public static String combine(String str1, String str2) {
        return str1 + " - " + str2;
    }
}
