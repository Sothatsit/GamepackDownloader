package net.sothatsit.gamepackdownloader;

import java.io.*;
import java.net.URL;
import java.util.Arrays;

public class GamePackDownloader {

    public static final String GAMEPACK_URL = "http://oldschool1.runescape.com/gamepack.jar";

    public static void main(String[] args) {
        run(args);
    }

    public static void run(String[] args) {
        if(args.length == 0) {
            exit("Output folder not supplied");
            return;
        }

        if(args.length != 1) {
            exit("More arguments supplied than necessary");
            return;
        }

        File folder = new File(args[0]);

        if(!folder.exists()) {
            exit("Supplied file does not exist");
            return;
        }

        if(!folder.isDirectory()) {
            exit("Supplied file is not a directory");
            return;
        }

        int latest = getLatestVersion(folder);

        info("Current Version: v" + latest);

        File file = (latest < 0 ? null : new File(folder, "gamepack " + latest + ".jar"));
        File newFile = new File(folder, "gamepack " + (latest < 0 ? 1 : latest + 1) + ".jar");
        File output = new File(folder, "gamepack temp.jar");

        try {
            URL url = new URL(GAMEPACK_URL);

            final URLDownloader downloader = new URLDownloader(url, output);
            final long start = System.nanoTime();

            new Thread() {
                @Override
                public void run() {
                    while(!downloader.isFinished()) {
                        try {
                            long progress = downloader.getProgress();
                            long size = downloader.getFileSize();

                            if(progress < 0) {
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
        } catch(IOException e) {
            e.printStackTrace();
        }

        if(file != null) {
            info("Checking file against previous version");

            final long start = System.nanoTime();

            boolean same;
            try {
                same = areContentsSame(file, output);
            } catch (IOException e) {
                info("Error checking contents of files against eachother");
                e.printStackTrace();
                exit();
                return;
            }

            info("Files Checked [" + round((System.nanoTime() - start) / 1000000d, 2) + "ms]");

            if(same) {
                info("Contents of files are the same, deleting newly downloaded file");

                boolean deleted = output.delete();

                if(!deleted) {
                    exit("Unable to delete output file");
                    return;
                }

                log("output deleted");
                exit("Current Version: v" + getLatestVersion(folder));
                return;
            } else {
                if(output.renameTo(newFile)) {
                    info("output renamed from temp");
                } else {
                    info("unable to rename output from temp");
                }
            }
        }

        exit("New Version Downloaded: v" + getLatestVersion(folder));
    }

    public static int getLatestVersion(File folder) {
        File[] files;
        if(folder == null || !folder.exists() || (files  = folder.listFiles()) == null || files.length == 0) {
            return -1;
        }

        int highest = -1;
        for(File f : files) {
            int v;
            if((v = getVersion(f.getName())) > highest) {
                highest = v;
            }
        }
        return highest;
    }

    public static int getVersion(String fileName) {
        if(fileName.startsWith("gamepack ") && fileName.endsWith(".jar")) {
            try {
                String version = fileName.substring(9, fileName.length() - 4);

                Integer v = Integer.valueOf(version);

                return v;
            } catch(NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }

    public static boolean areContentsSame(File f1, File f2) throws IOException {
        if(f1 == null || f2 == null || !f1.exists() || !f2.exists() || f1.length() != f2.length()) {
            return false;
        }

        BufferedInputStream stream1 = null;
        BufferedInputStream stream2 = null;

        try {
            stream1 = new BufferedInputStream(new FileInputStream(f1));
            stream2 = new BufferedInputStream(new FileInputStream(f1));

            byte[] data1 = new byte[1024];
            byte[] data2 = new byte[1024];

            while(stream1.read(data1) != -1 && stream2.read(data2) != -1) {
                if(!Arrays.equals(data1, data2)) {
                    return false;
                }
            }

            return true;
        } finally {
            if(stream1 != null) {
                stream1.close();
            }

            if(stream2 != null) {
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
        log("[Info] "+log);
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
