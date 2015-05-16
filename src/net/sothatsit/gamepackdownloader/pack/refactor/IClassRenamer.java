package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IClassRenamer {

    public String getNewName(int version, int access, String name, String signature, String superName, String[] interfaces);

}
