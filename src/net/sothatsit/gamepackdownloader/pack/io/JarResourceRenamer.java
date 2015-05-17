package net.sothatsit.gamepackdownloader.pack.io;

import net.sothatsit.gamepackdownloader.pack.refactor.BaseRefactorer;

import java.util.HashMap;
import java.util.Map;

public class JarResourceRenamer extends BaseRefactorer {

    private Map<String, String> oldClassToNew = new HashMap<>();

    @Override
    public String getNewName(int version, int access, String name, String signature, String superName, String[] interfaces) {
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
        String clazz = oldClassToNew.getOrDefault(className, className);

        BasicYAML yaml = BasicYAML.getFile(clazz);

        String origName = super.getNewName(className, access, name, desc, signature, exceptions);

        if(yaml != null && yaml.isSet(origName)) {
            return yaml.getValue(origName);
        }

        return origName;
    }

}
