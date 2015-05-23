package net.sothatsit.gamepackdownloader.util;

import java.util.Arrays;
import java.util.List;

public class JavaUtil {

    public static final List<String> JAVA_KEYWORDS = Arrays.asList("long", "int", "boolean", "char", "double", "float", "const"
            , "public", "protected", "private", "static", "abstract", "interface", "class", "import", "package", "final", "transient"
            , "enum", "extends", "for", "while", "continue", "break", "do", "try", "catch", "finally", "return", "synchronized"
            , "if", "else", "assert", "switch", "case", "goto", "implements", "instanceof", "native", "new", "this", "throw"
            , "throws", "void", "volatile", "false", "null", "true");

    public static boolean isJavaKeyword(String name) {
        return JAVA_KEYWORDS.contains(name);
    }

}
