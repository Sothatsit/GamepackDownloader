package net.sothatsit.gamepackdownloader.refactor;

public interface IFieldRenamer {

    public String getNewName(String className, int access, String name, String desc, String signature, Object value);

}
