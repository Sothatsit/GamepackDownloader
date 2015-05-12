package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IFieldRenamer {

    public String getNewName(String className, String oldName, String descriptor);

}
