package net.sothatsit.gamepackdownloader.pack.refactor;

public interface IMethodRenamer {

    public String getNewName(String className, int access, String name, String desc, String signature, String[] exceptions);

}
