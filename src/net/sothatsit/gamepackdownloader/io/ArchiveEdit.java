package net.sothatsit.gamepackdownloader.io;

public class ArchiveEdit {

    private final String fileName;
    private final byte[] contents;

    public ArchiveEdit(byte[] contents) {
        this.fileName = null;
        this.contents = contents;
    }

    public ArchiveEdit(String fileName, byte[] contents) {
        this.fileName = fileName;
        this.contents = contents;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getContents() {
        return contents;
    }

}
