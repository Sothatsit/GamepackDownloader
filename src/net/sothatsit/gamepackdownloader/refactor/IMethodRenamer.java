package net.sothatsit.gamepackdownloader.refactor;

public interface IMethodRenamer {

    public String getNewName(String className, int access, String name, String desc, String signature, String[] exceptions);

}
