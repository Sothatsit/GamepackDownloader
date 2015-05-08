package net.sothatsit.gamepackdownloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class GamePackChecker {

    public static final String GAME_URL = "http://oldschool1.runescape.com/";
    public static final Split[] splits = {
            new Split("document.write('archive=", 1),
            new Split(" ');", 0)
    };

    public static void check(int threadCount, int reloadCount) {
        log("GamePack Checker Started");

        final List<String> gamePacks = new ArrayList<>();
        final List<GamePackCheckThread> threads = new ArrayList<>();

        for(int i = 0; i < threadCount; i++) {
            GamePackCheckThread thread = new GamePackCheckThread(gamePacks, threads, reloadCount);

            synchronized (threads) {
                threads.add(thread);
            }

            thread.start();
        }

        while(threads.size() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        log("Loaded " + gamePacks.size() + " unique GamePacks");

        exit();
    }

    public static class GamePackCheckThread extends Thread {

        private final List<String> gamePacks;
        private final List<GamePackCheckThread> threads;
        private final int reloadCount;

        public GamePackCheckThread(List<String> gamePacks, List<GamePackCheckThread> threads, int reloadCount) {
            this.gamePacks = gamePacks;
            this.threads = threads;
            this.reloadCount = reloadCount;
        }

        @Override
        public void run() {
            try {
                for(int i = 0; i < reloadCount; i++) {
                    String gamePack = getGamePack();

                    if(gamePack == null) {
                        continue;
                    }

                    synchronized (gamePacks) {
                        boolean duplicate = gamePacks.contains(gamePack);

                        log("Loaded gamePack: " + gamePack + " (duplicate ? " + duplicate + ")");

                        if(!duplicate) {
                            gamePacks.add(gamePack);
                        }
                    }
                }
            } finally {
                synchronized (threads) {
                    threads.remove(this);
                }
            }
        }

    }

    public static String getGamePack() {
        URL url;
        try {
            url = new URL(GAME_URL);
        } catch (MalformedURLException e) {
            log("URL Malformed: \"" + GAME_URL + "\"");
            e.printStackTrace();
            exit();
            return null;
        }

        log("Downloading web page content...");

        long start = System.nanoTime();
        String content;
        try {
            content = getContent(url);
        } catch (IOException e) {
            log("Error getting content of web page: \"" + GAME_URL + "\"");
            e.printStackTrace();
            exit();
            return null;
        }

        log("Recieved web page content [" + (long) ((System.nanoTime() - start) / 1000000d) + "ms]");

        //info("content: " + content);

        for(Split split : splits) {
            String[] str = splitAtFirst(content, split.splitAt);

            if(str.length <= split.index) {
                log("Invalid Index: " + split.index);
                log("str.length: " + str.length);
                log("splitAt: " + split.splitAt);
                continue;
            }

            content = str[split.index];
        }

        return content;
    }

    public static String[] splitAtFirst(String subject, String splitAt) {
        if(subject == null || subject.length() == 0) {
            return new String[] {"", ""};
        }

        if(splitAt == null || splitAt.length() == 0) {
            return new String[] {subject, ""};
        }

        char[] subjectArray = subject.toCharArray();
        char[] split = splitAt.toCharArray();

        StringBuilder builder = null;
        for(int i = 0; i < subjectArray.length; i++) {
            char c = subjectArray[i];

            if(builder == null && c == split[0]) {
                builder = new StringBuilder();
            }

            if(builder != null) {
                builder.append(c);

                if(startsWith(splitAt, builder.toString())) {
                    if(builder.length() == splitAt.length()) {
                        return new String[] {subject.substring(0, i - builder.length() + 1), subject.substring(i + 1)};
                    }
                } else {
                    builder = null;
                }
            }
        }

        return new String[] {subject, ""};
    }

    public static boolean startsWith(String subject, String start) {
        if(subject == null || subject.length() == 0 || start == null || start.length() == 0 || start.length() > subject.length()) {
            return false;
        }

        char[] c1 = subject.toCharArray();
        char[] c2 = start.toCharArray();
        for(int i = 0; i < c2.length; i++) {
            if(c1[i] != c2[i]) {
                return false;
            }
        }

        return true;
    }

    public static String getContent(URL url) throws IOException {
        BufferedReader rd = null;

        try {
            URLConnection conn = url.openConnection();

            if(conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
            }

            HttpURLConnection.setFollowRedirects(false);

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }

            return sb.toString();

        } finally {
            if(rd != null) {
                rd.close();
            }
        }
    }

    public static class Split {
        public String splitAt;
        public int index;

        public Split(String splitAt, int index) {
            this.splitAt = splitAt;
            this.index = index;
        }
    }

    public static void log(String log) {
        System.out.println(log);
    }

    public static void exit() {
        System.exit(0);
    }
}
