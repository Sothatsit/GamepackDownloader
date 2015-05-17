package net.sothatsit.gamepackdownloader.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class JarArchive {

    private File jarFile;
    private List<Entry> entries;

    public JarArchive(File jarFile) throws IOException {
        this.jarFile = jarFile;
    }

    public void loadArchive(ArchiveLoader loader) throws IOException {
        loadEntriesList();

        FileInputStream fis = null;
        ZipInputStream zip = null;

        try {
            fis = new FileInputStream(jarFile);
            zip = new ZipInputStream(fis);

            int offset = 0;
            ZipEntry entry;
            while((entry = zip.getNextEntry()) != null) {
                long size = entry.getSize();

                if (size < 0) {
                    for(Entry e : entries) {
                        if(e.getName().equals(entry.getName())) {
                            size = e.getSize();
                            break;
                        }
                    }

                    if(size < 0) {
                        continue;
                    }
                }

                if(!entry.isDirectory() && loader.shouldLoad(jarFile, entry)) {
                    byte[] data = new byte[(int) size];

                    int read = zip.read(data, offset, (int) size);

                    if(read < 0) {
                        break;
                    }

                    loader.onLoad(jarFile, entry, data);
                }

                offset += size;
            }
        } finally {
            if(fis != null) {
                fis.close();
            }

            if(zip != null) {
                zip.close();
            }
        }
    }

    public void editArchive(final ArchiveEditor editor) throws IOException {
        FileOutputStream fos = null;
        ZipOutputStream zip = null;
        try {
            String path = jarFile.getAbsolutePath();
            int lastIndex = path.lastIndexOf('.');

            String name = path.substring(0, lastIndex - 1) + " temp" + path.substring(lastIndex);

            fos = new FileOutputStream(new File(name));
            zip = new ZipOutputStream(fos);
            final ZipOutputStream stream = zip;

            ArchiveLoader loader = new ArchiveLoader() {
                @Override
                public boolean shouldLoad(File file, ZipEntry entry) throws IOException {
                    return true;
                }

                @Override
                public void onLoad(File file, ZipEntry entry, byte[] data) throws IOException {
                    if(editor.shouldEdit(file, entry)) {
                        data = editor.edit(file, entry, data);
                    }

                    ZipEntry newEntry = new ZipEntry(entry.getName());
                    stream.putNextEntry(newEntry);
                    stream.write(data);
                }
            };

            loadArchive(loader);
        } finally {
            if(fos != null) {
                fos.close();
            }

            if(zip != null) {
                zip.close();
            }
        }
    }

    public void loadEntriesList() throws IOException{
        this.entries = new ArrayList<>();
        ZipFile zipFile = null;

        try {
            zipFile = new ZipFile(jarFile);

            Enumeration<? extends ZipEntry> entriesEnum = zipFile.entries();
            while(entriesEnum.hasMoreElements()) {
                ZipEntry entry = entriesEnum.nextElement();

                this.entries.add(new Entry(entry));
            }
        } finally {
            if(zipFile != null) {
                zipFile.close();
            }
        }
    }

    public List<Entry> getEntriesList() throws IOException {
        if(entries == null) {
            this.loadEntriesList();
        }

        return new ArrayList<>(entries);
    }

    public boolean isEntriesListLoaded() {
        return entries != null;
    }

    public class Entry {
        private String name;
        private long size;

        public Entry(ZipEntry entry) {
            this.name = entry.getName();
            this.size = entry.getSize();
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }
    }
}