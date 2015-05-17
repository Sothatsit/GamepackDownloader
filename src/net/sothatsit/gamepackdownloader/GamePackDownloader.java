package net.sothatsit.gamepackdownloader;

import de.fernflower.main.decompiler.ConsoleDecompiler;
import net.sothatsit.gamepackdownloader.io.JarArchive;
import net.sothatsit.gamepackdownloader.io.JarResourceRenamer;
import net.sothatsit.gamepackdownloader.refactor.asm.JarRefactorer;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class GamePackDownloader {

    public static final String REPLACE = "%archive%";
    public static final String GAMEPACK_URL = "http://oldschool1.runescape.com/" + REPLACE;

    public static void main(String[] args) {
        run(args);
    }

    public static String getGamepackUrl() {
        String archive = GamePackChecker.getGamePack();
        return archive == null ? "" : GAMEPACK_URL.replace(REPLACE, archive);
    }

    public static void run(String[] args) {
        if (args.length < 2) {
            log("Not enough arguments supplied");
            exit("args: [gamepack-output] [options] [fernflower-options]");
            return;
        }

        File folder = new File(args[0]);

        if (!folder.exists()) {
            exit("Supplied file does not exist");
            return;
        }

        if (!folder.isDirectory()) {
            exit("Supplied file is not a directory");
            return;
        }

        int latest = getLatestVersion(folder);

        if (latest > 0) {
            info("Current Version: v" + latest);
        }

        String options = args[1];

        if (options.length() < 2 || options.charAt(0) != '-') {
            log("Invalid options supplied: " + options);
            exit("Valid: -<d (download) , r (refactor) , s (decompile)>");
            return;
        }

        boolean download = false;
        boolean refactor = false;
        boolean decompile = false;

        for (char c : options.substring(1).toCharArray()) {
            if (c == 'd') {
                download = true;
            } else if (c == 's') {
                decompile = true;
            } else if(c == 'r') {
                refactor = true;
            } else {
                exit("Unknown option: '" + c + '\'');
                return;
            }
        }

        if (download && !downloadLatest(folder)) {
            if (decompile || refactor) {
                exit("No new versions to " + (decompile ? (refactor ? "refactor or decompile" : "decompile") : "refactor") + ", exiting");
            } else {
                exit();
            }
            return;
        }

        File refactored = null;

        if(refactor) {
            refactored = refactor(folder);
        }

        if (decompile) {
            String[] arguments = new String[args.length - 2];

            System.arraycopy(args, 2, arguments, 0, args.length - 2);

            int latestVersion = getLatestVersion(folder);
            File file = new File(folder, "gamepack " + latestVersion + ".jar");

            decompileFile((refactored == null ? file : refactored), latestVersion, arguments);
        }

        exit();
    }

    public static File refactor(File folder) {
        try {
            int latest = getLatestVersion(folder);
            File jarFile = new File(folder, "gamepack " + latest + ".jar");
            File refactored = new File(folder, "gamepack " + latest + " refactored.jar");

            info("Copying Gamepack " + latest);

            Files.copy(jarFile.toPath(), refactored.toPath());

            info("Refactoring Gamepack " + latest);

            JarArchive archive = new JarArchive(refactored);
            JarResourceRenamer nameSupplier = new JarResourceRenamer();

            JarRefactorer.refactor(archive, nameSupplier, nameSupplier, nameSupplier);

            info("Refactored Gamepack " + latest);

            return refactored;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void decompileFile(File file, int version, String[] args) {
        info("Decompiling Gamepack " + version + " using fernflower");

        File f = new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4) + " source");

        if (f.exists() && f.isDirectory()) {
            info("Deleting previous source code to avoid conflicts");
            deleteFolder(f);
        } else if (f.exists()) {
            info("Deleting previous source code to avoid conflicts");
            f.delete();
        }

        f.mkdir();

        String[] arguments = new String[args.length + 2];

        System.arraycopy(args, 0, arguments, 0, args.length);

        arguments[arguments.length - 2] = file.getAbsolutePath();
        arguments[arguments.length - 1] = f.getAbsolutePath();

        runFernflower(arguments);

        info("Decompiled Gamepack " + version);

        File source = new File(f, file.getName());

        unZipIt(source, f);

        if (!source.delete()) {
            info("Unable to delete zip archive");
        }

        info("Un-zipped Gamepack " + version + " source");
    }

    public static void deleteFolder(File folder) {
        for (File subFile : folder.listFiles()) {
            if (subFile.isDirectory()) {
                deleteFolder(subFile);
            }

            subFile.delete();
        }
        folder.delete();
    }

    public static boolean downloadLatest(File folder) {
        int latest = getLatestVersion(folder);

        File file = (latest < 0 ? null : new File(folder, "gamepack " + latest + ".jar"));
        File newFile = new File(folder, "gamepack " + (latest < 0 ? 1 : latest + 1) + ".jar");
        File output = new File(folder, "gamepack temp.jar");

        if(output.exists() && output.delete()) {
            info("Deleted gamepack temp");
        }

        try {
            URL url = new URL(getGamepackUrl());

            final URLDownloader downloader = new URLDownloader(url, output);
            final long start = System.nanoTime();

            new Thread() {
                @Override
                public void run() {
                    while (!downloader.isFinished()) {
                        try {
                            long progress = downloader.getProgress();
                            long size = downloader.getFileSize();

                            if (progress < 0 || size < 0) {
                                continue;
                            }

                            long ms = (long) ((System.nanoTime() - start) / 1000000d);
                            long seconds = ms / 1000l;

                            double downloadSpeed = toKB((double) progress / (double) seconds);
                            double percentage = round((double) progress / (double) size * 100d, 2);

                            log(toKB(progress) + "kb / " + toKB(size) + "kb (" + percentage + "%) " + downloadSpeed + "kb/s");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();

            downloader.download();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            info("Finished Downloading [" + (long) ((System.nanoTime() - start) / 1000000d) + "ms]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file != null) {
            info("Checking file against previous version");

            final long start = System.nanoTime();

            boolean same;
            try {
                same = areContentsSame(file, output);
            } catch (IOException e) {
                info("Error checking contents of files against eachother");
                e.printStackTrace();
                exit();
                return false;
            }

            info("Files Checked [" + round((System.nanoTime() - start) / 1000000d, 2) + "ms]");

            if (same) {
                info("Contents of files are the same, deleting newly downloaded file");

                boolean deleted = output.delete();

                if (!deleted) {
                    exit("Unable to delete output file");
                    return false;
                }

                info("output deleted");
                exit("Current Version: v" + getLatestVersion(folder));
                return false;
            } else {
                if (output.renameTo(newFile)) {
                    info("output renamed from temp");
                } else {
                    info("unable to rename output from temp");
                    return false;
                }
            }
        } else {
            if (output.renameTo(newFile)) {
                info("output renamed from temp");
            } else {
                info("unable to rename output from temp");
                return false;
            }
        }

        info("New Version Downloaded: v" + getLatestVersion(folder));
        return true;
    }

    public static void runFernflower(String[] args) {
        ConsoleDecompiler.main(args);
    }

    public static int getLatestVersion(File folder) {
        File[] files;
        if (folder == null || !folder.exists() || (files = folder.listFiles()) == null || files.length == 0) {
            return -1;
        }

        int highest = -1;
        for (File f : files) {
            int v;
            if ((v = getVersion(f.getName())) > highest) {
                highest = v;
            }
        }
        return highest;
    }

    public static int getVersion(String fileName) {
        if (fileName.startsWith("gamepack ") && fileName.endsWith(".jar")) {
            try {
                String version = fileName.substring(9, fileName.length() - 4);

                return Integer.valueOf(version);
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
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

    public static double toKB(double bytes) {
        return round(bytes / 1024d, 2);
    }

    public static double round(double num, int places) {
        return (double) ((int) (num * Math.pow(10, places))) / Math.pow(10, places);
    }

    public static void info(String log) {
        log("[Info] " + log);
    }

    public static void log(String log) {
        System.out.println(log);
    }

    public static void exit(String log) {
        info(log);
        exit();
    }

    public static void exit() {
        System.exit(0);
    }
}
