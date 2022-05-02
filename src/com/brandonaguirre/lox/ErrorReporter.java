package com.brandonaguirre.lox;

public class ErrorReporter {
    private static boolean hasError = false;

    public static boolean hadError() {
        return hasError;
    }

    public static void resetError() {
        hasError = false;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println(
            "[line " + line + "] Error" + where + ": " + message);
        hasError = true;
    }
}
