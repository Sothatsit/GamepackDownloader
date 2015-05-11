package net.sothatsit.gamepackdownloader.bytecode;

public interface IClassRenamer {

    public String getNewName(String oldName, String[] interfaces, String superClass);

}
