package net.sothatsit.gamepackdownloader;

import jdk.internal.org.objectweb.asm.tree.ClassNode;
import net.sothatsit.gamepackdownloader.refactorer.JarRefactorer;
import net.sothatsit.gamepackdownloader.refactorer.SingleRefactorAgent;
import net.sothatsit.gamepackdownloader.refactorer.RefactorMap;
import net.sothatsit.gamepackdownloader.util.JarUtil;
import net.sothatsit.gamepackdownloader.util.Log;
import net.sothatsit.gamepackdownloader.util.LogLevel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

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
            Log.log("Not enough arguments supplied");
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
            Log.info("Current Version: v" + latest);
        }

        String options = args[1];

        if (options.length() < 2 || options.charAt(0) != '-') {
            Log.log("Invalid options supplied: " + options);
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

            FernFlowerHandler.decompileFile((refactored == null ? file : refactored), latestVersion, arguments);
        }

        exit();
    }

    public static File refactor(File folder) {
        int latest = getLatestVersion(folder);
        File jarFile = new File(folder, "gamepack " + latest + ".jar");
        File refactored = new File(folder, "gamepack " + latest + " refactored.jar");

        if(refactored.exists()) {
            Log.info("Refactored jar exists, deleting");
            refactored.delete();
        }

        Log.info("Refactoring Gamepack " + latest);

        Log.setLogLevel(LogLevel.BASIC);

        Map<String, ClassNode> classes = JarUtil.loadClasses(jarFile);
        RefactorMap map = SingleRefactorAgent.refactor(new ArrayList<>(classes.values()));

        JarRefactorer.refactor(jarFile, refactored, map);

        //JarArchive archive = new JarArchive(refactored);
        //JarResourceRenamer nameSupplier = new JarResourceRenamer();
        //
        //JarRefactorer.refactor(archive, nameSupplier, nameSupplier, nameSupplier);

        Log.info("Refactored Gamepack " + latest);

        return refactored;
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
            Log.info("Deleted gamepack temp");
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

                            Log.log(toKB(progress) + "kb / " + toKB(size) + "kb (" + percentage + "%) " + downloadSpeed + "kb/s");
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

            Log.info("Finished Downloading [" + (long) ((System.nanoTime() - start) / 1000000d) + "ms]");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (file != null) {
            Log.info("Checking file against previous version");

            final long start = System.nanoTime();

            boolean same;
            try {
                same = JarUtil.areContentsSame(file, output);
            } catch (IOException e) {
                Log.info("Error checking contents of files against eachother");
                e.printStackTrace();
                exit();
                return false;
            }

            Log.info("Files Checked [" + round((System.nanoTime() - start) / 1000000d, 2) + "ms]");

            if (same) {
                Log.info("Contents of files are the same, deleting newly downloaded file");

                boolean deleted = output.delete();

                if (!deleted) {
                    exit("Unable to delete output file");
                    return false;
                }

                Log.info("output deleted");
                exit("Current Version: v" + getLatestVersion(folder));
                return false;
            } else {
                if (output.renameTo(newFile)) {
                    Log.info("output renamed from temp");
                } else {
                    Log.info("unable to rename output from temp");
                    return false;
                }
            }
        } else {
            if (output.renameTo(newFile)) {
                Log.info("output renamed from temp");
            } else {
                Log.info("unable to rename output from temp");
                return false;
            }
        }

        Log.info("New Version Downloaded: v" + getLatestVersion(folder));
        return true;
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

    public static double toKB(double bytes) {
        return round(bytes / 1024d, 2);
    }

    public static double round(double num, int places) {
        return (double) ((int) (num * Math.pow(10, places))) / Math.pow(10, places);
    }

    public static void exit(String log) {
        Log.info(log);
        exit();
    }

    public static void exit() {
        System.exit(0);
    }
}
