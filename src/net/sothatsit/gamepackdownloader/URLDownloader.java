package net.sothatsit.gamepackdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

public class URLDownloader {

    private URL url;
    private File output;
    private long fileSize;
    private FileChannel channel;

    public URLDownloader(URL url, File output) {
        this.url = url;
        this.output = output;
        this.fileSize = -1;
    }

    public void download() throws IOException {
        this.fileSize = getFileSizeFromURL();
        this.downloadFromURL();
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getProgress() throws IOException {
        if (output == null || !output.exists()) {
            return -1;
        }

        return output.length();
    }

    public boolean isFinished() {
        return (channel != null && !channel.isOpen());
    }

    private long getFileSizeFromURL() throws IOException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.getInputStream();
            return conn.getContentLength();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private void downloadFromURL() throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        FileOutputStream fos = new FileOutputStream(output);

        this.channel = fos.getChannel();
        this.channel.transferFrom(rbc, 0, Long.MAX_VALUE);
    }

}
