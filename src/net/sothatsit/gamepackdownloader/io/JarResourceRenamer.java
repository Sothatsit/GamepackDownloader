package net.sothatsit.gamepackdownloader.io;

import net.sothatsit.gamepackdownloader.refactor.BaseRefactorer;
import net.sothatsit.gamepackdownloader.refactor.descriptor.ClassNameSupplier;

import java.util.HashMap;
import java.util.Map;

public class JarResourceRenamer extends BaseRefactorer implements ClassNameSupplier {

    private Map<String, String> oldClassToNew = new HashMap<>();
    private boolean hitRoadBlock = false;
    private boolean refactorFields = true;
    private boolean refactorMethods = true;

    public void softReset() {
        oldClassToNew = new HashMap<>();
        hitRoadBlock = false;
    }

    public void hardReset() {
        oldClassToNew = new HashMap<>();
        hitRoadBlock = false;
        refactorFields = true;
        refactorMethods = true;
    }

    public boolean hasHitRoadBlock() {
        return hitRoadBlock;
    }

    public void setRefactorFields(boolean refactorFields) {
        this.refactorFields = refactorFields;
    }

    public void setRefactorMethods(boolean refactorMethods) {
        this.refactorMethods = refactorMethods;
    }

    public boolean isRefactorFields() {
        return refactorFields;
    }

    public boolean isRefactorMethods() {
        return refactorMethods;
    }

    @Override
    public String getNewName(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if(!oldClassToNew.containsKey(superName)) {
            hitRoadBlock = true;
            return name;
        }

        for(String str : interfaces) {
            if(!oldClassToNew.containsKey(str)) {
                hitRoadBlock = true;
                return name;
            }
        }

        String origName = super.getNewName(version, access, name, signature, superName, interfaces);

        BasicYAML yaml = BasicYAML.getFile(origName);

        if(yaml != null && yaml.isSet("class-name")) {
            String newName = yaml.getValue("class-name");

            oldClassToNew.put(name, newName);
            return newName;
        }

        oldClassToNew.put(name, origName);
        return origName;
    }

    @Override
    public String getNewName(String className, int access, String name, String desc, String signature, Object value) {
        if(!refactorFields) {
            return name;
        }

        String clazz = oldClassToNew.getOrDefault(className, className);

        BasicYAML yaml = BasicYAML.getFile(clazz);

        String origName = super.getNewName(className, access, name, desc, signature, value);

        if(yaml != null && yaml.isSet(origName)) {
            return yaml.getValue(origName);
        }

        return origName;
    }

    @Override
    public String getNewName(String className, int access, String name, String desc, String signature, String[] exceptions) {
        if(!refactorMethods) {
            return name;
        }

        String clazz = oldClassToNew.getOrDefault(className, className);

        BasicYAML yaml = BasicYAML.getFile(clazz);

        String origName = super.getNewName(className, access, name, desc, signature, exceptions);

        if(yaml != null && yaml.isSet(origName)) {
            return yaml.getValue(origName);
        }

        return origName;
    }

    @Override
    public String getClassName(String oldName) {
        return oldClassToNew.getOrDefault(oldName, oldName);
    }
}
