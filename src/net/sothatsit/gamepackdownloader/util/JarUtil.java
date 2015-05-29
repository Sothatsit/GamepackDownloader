package net.sothatsit.gamepackdownloader.util;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarUtil {

    public static Map<String, ClassNode> loadClasses(File f) {
        Log.info("Loading Jar File \"" + f + "\"'s Classes");

        Map<String, ClassNode> map = new HashMap<>();

        FileInputStream fis = null;
        ZipInputStream zis = null;
        ByteOutputStream bos = null;
        try {
            fis = new FileInputStream(f);
            zis = new ZipInputStream(fis);

            ZipEntry entry;
            while((entry = zis.getNextEntry()) != null) {
                if(!entry.getName().endsWith(".class")) {
                    continue;
                }

                bos = new ByteOutputStream();

                int size;
                byte[] buffer = new byte[2048];
                while((size = zis.read(buffer, 0, buffer.length)) != -1) {
                    bos.write(buffer, 0, size);
                }

                ClassReader reader = new ClassReader(bos.getBytes());
                ClassNode node = new ClassNode(Opcodes.ASM5);
                reader.accept(node, 0);

                map.put(entry.getName(), node);

                Log.loadedEntry(entry.getName());

                bos.close();
                bos = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            close(fis, "FileInputStream");
            close(zis, "ZipInputStream");
            close(bos, "ByteOutputStream");

        }

        Log.info("Successfully loaded " + map.size() + " classes");

        return map;
    }

    public static void close(Object stream, String name) {
        if(stream == null) {
            return;
        }

        try{
            if(stream instanceof InputStream) {
                ((InputStream) stream).close();
            } else if(stream instanceof OutputStream) {
                ((OutputStream) stream).flush();
                ((OutputStream) stream).close();
            }
        } catch(IOException e) {
            Log.error("Error closing " + name);
            e.printStackTrace();
        }
    }

    public static void unZipIt(File zipFile, File outputFolder) {
        byte[] buffer = new byte[1024];

        try {
            if (!outputFolder.exists()) {
                outputFolder.mkdir();
            }

            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);

                System.out.println("file unzip : " + newFile.getAbsoluteFile());

                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();

            System.out.println("Done");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean areContentsSame(File f1, File f2) throws IOException {
        if (f1 == null || f2 == null || !f1.exists() || !f2.exists() || f1.length() != f2.length()) {
            return false;
        }

        BufferedInputStream stream1 = null;
        BufferedInputStream stream2 = null;

        try {
            stream1 = new BufferedInputStream(new FileInputStream(f1));
            stream2 = new BufferedInputStream(new FileInputStream(f1));

            byte[] data1 = new byte[1024];
            byte[] data2 = new byte[1024];

            while (stream1.read(data1) != -1 && stream2.read(data2) != -1) {
                if (!Arrays.equals(data1, data2)) {
                    return false;
                }
            }

            return true;
        } finally {
            if (stream1 != null) {
                stream1.close();
            }

            if (stream2 != null) {
                stream2.close();
            }
        }
    }

}
