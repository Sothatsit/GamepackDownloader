package net.sothatsit.gamepackdownloader.util;

import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;
import jdk.internal.org.objectweb.asm.ClassReader;
import jdk.internal.org.objectweb.asm.Opcodes;
import jdk.internal.org.objectweb.asm.tree.ClassNode;

import java.io.*;
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

                Log.info("Loaded Entry " + entry.getName());

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
                ((OutputStream) stream).close();
            }
        } catch(IOException e) {
            Log.error("Error closing " + name);
            e.printStackTrace();
        }
    }

}
