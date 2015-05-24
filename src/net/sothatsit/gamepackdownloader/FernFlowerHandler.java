package net.sothatsit.gamepackdownloader;

import de.fernflower.main.decompiler.ConsoleDecompiler;
import net.sothatsit.gamepackdownloader.util.JarUtil;
import net.sothatsit.gamepackdownloader.util.Log;

import java.io.File;

public class FernFlowerHandler {

    public static void decompileFile(File file, int version, String[] args) {
        Log.info("Decompiling Gamepack " + version + " using fernflower");

        File f = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4) + " source");

        if (f.exists() && f.isDirectory()) {
            Log.info("Deleting previous source code to avoid conflicts");
            GamePackDownloader.deleteFolder(f);
        } else if (f.exists()) {
            Log.info("Deleting previous source code to avoid conflicts");
            f.delete();
        }

        f.mkdir();

        String[] arguments = new String[args.length + 2];

        System.arraycopy(args, 0, arguments, 0, args.length);

        arguments[arguments.length - 2] = file.getAbsolutePath();
        arguments[arguments.length - 1] = f.getAbsolutePath();

        runFernflower(arguments);

        Log.info("Decompiled Gamepack " + version);

        File source = new File(f, file.getName());

        JarUtil.unZipIt(source, f);

        if (!source.delete()) {
            Log.info("Unable to delete zip archive");
        }

        Log.info("Un-zipped Gamepack " + version + " source");
    }

    public static void runFernflower(String[] args) {
        ConsoleDecompiler.main(args);
    }

}
