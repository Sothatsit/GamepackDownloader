package net.sothatsit.gamepackdownloader.bytecode;

public interface IMethodRenamer {

    public String getNewName(String className, String oldName, String descriptor);

}
