package edu.ufl.cise.plpfa22;

public class StringUtil {
    public static boolean not(boolean arg) {
        return !arg;
    }

    public static boolean lessThanString(String a, String b) {
        return b.startsWith(a) && !b.equals(a);
    }

    public static boolean greaterThanString(String a, String b) {
        return a.endsWith(b) && !a.equals(b);
    }

    public static boolean greaterAndEqualString(String a, String b) {
        return a.endsWith(b);
    }

    public static boolean lesserAndEqualString(String a, String b) {
        return b.startsWith(a);
    }
}
