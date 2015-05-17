package net.sothatsit.gamepackdownloader.io;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;

public interface ArchiveLoader {

    public boolean shouldLoad(File file, ZipEntry entry) throws IOException;

    public void onLoad(File file, ZipEntry entry, byte[] data) throws IOException;

}
