package net.sothatsit.gamepackdownloader.pack.asm;

import java.io.File;
import java.util.zip.ZipEntry;

public interface ArchiveLoader {

    public boolean shouldLoad(File file, ZipEntry entry);

    public void onLoad(File file, ZipEntry entry, byte[] data);

}
