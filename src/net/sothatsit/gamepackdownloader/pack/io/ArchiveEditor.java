package net.sothatsit.gamepackdownloader.pack.io;

import java.io.File;
import java.util.zip.ZipEntry;

public interface ArchiveEditor {

    public boolean shouldEdit(File file, ZipEntry entry);

    public byte[] edit(File file, ZipEntry entry, byte[] data);

}
