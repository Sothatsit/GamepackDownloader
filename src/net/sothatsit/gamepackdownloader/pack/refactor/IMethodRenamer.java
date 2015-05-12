package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IMethodRenamer {

    public String getNewName(String className, String oldName, String descriptor);

}
