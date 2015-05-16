package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IFieldRenamer {

    public String getNewName(String className, int access, String name, String desc, String signature, Object value);

}
