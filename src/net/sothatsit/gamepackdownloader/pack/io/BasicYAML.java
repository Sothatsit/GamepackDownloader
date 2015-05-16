package net.sothatsit.gamepackdownloader.pack.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class BasicYAML {

    private static final Map<String, BasicYAML> files = new HashMap<>();
    private Map<String, String> values;

    public BasicYAML(InputStream is) {
        this.values = new HashMap<>();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = br.readLine()) != null) {
                decodeLine(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void decodeLine(String line) {
        if (!containsCharacter(line, ':')) {
            return;
        }

        String[] split = splitAtFirst(line, ":");

        split[0] = split[0].trim();
        split[1] = split[1].trim();

        if (split[0].length() == 0 || split[1].length() == 0) {
            return;
        }

        values.put(split[0], split[1]);
    }

    public Map<String, String> getValues() {
        return values;
    }

    public boolean isSet(String key) {
        return values.containsKey(key);
    }

    public String getValue(String key) {
        return values.get(key);
    }

    public static String[] splitAtFirst(String subject, String splitAt) {
        if (subject == null || subject.length() == 0) {
            return new String[]{"", ""};
        }

        if (splitAt == null || splitAt.length() == 0) {
            return new String[]{subject, ""};
        }

        char[] subjectArray = subject.toCharArray();
        char[] split = splitAt.toCharArray();

        StringBuilder builder = null;
        for (int i = 0; i < subjectArray.length; i++) {
            char c = subjectArray[i];

            if (builder == null && c == split[0]) {
                builder = new StringBuilder();
            }

            if (builder != null) {
                builder.append(c);

                if (startsWith(splitAt, builder.toString())) {
                    if (builder.length() == splitAt.length()) {
                        return new String[]{subject.substring(0, i - builder.length() + 1), subject.substring(i + 1)};
                    }
                } else {
                    builder = null;
                }
            }
        }

        return new String[]{subject, ""};
    }

    public static boolean startsWith(String subject, String start) {
        if (subject == null || subject.length() == 0 || start == null || start.length() == 0 || start.length() > subject.length()) {
            return false;
        }

        char[] c1 = subject.toCharArray();
        char[] c2 = start.toCharArray();
        for (int i = 0; i < c2.length; i++) {
            if (c1[i] != c2[i]) {
                return false;
            }
        }

        return true;
    }

    public static boolean containsCharacter(String subject, char character) {
        for (char c : subject.toCharArray()) {
            if (c == character) {
                return true;
            }
        }
        return false;
    }

    public static BasicYAML getFile(String resource) {
        if (files.containsKey(resource)) {
            return files.get(resource);
        }

        InputStream stream = BasicYAML.class.getResourceAsStream(resource);

        if (stream == null) {
            return null;
        }

        BasicYAML yaml = new BasicYAML(stream);

        files.put(resource, yaml);

        return yaml;
    }
}
