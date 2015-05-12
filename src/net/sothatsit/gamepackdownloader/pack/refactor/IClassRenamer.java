package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IClassRenamer {

    public String getNewName(String oldName, String[] interfaces, String superClass);

}
