package net.sothatsit.gamepackdownloader.bytecode;

public interface IFieldRenamer {

    public String getNewName(String className, String oldName, String descriptor);

}
