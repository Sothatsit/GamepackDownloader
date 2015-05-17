package net.sothatsit.gamepackdownloader.refactor;

public interface IClassRenamer {

    public String getNewName(int version, int access, String name, String signature, String superName, String[] interfaces);

}
