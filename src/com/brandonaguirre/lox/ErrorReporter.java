package com.brandonaguirre.lox;

public class ErrorReporter {
    private static boolean hasError = false;
    private static boolean hasRuntimeError = false;
    public static boolean hadError() {
        return hasError;
    }

    public static boolean hadRuntimeError() {
        return hasRuntimeError;
    }

    public static void resetError() {
        hasError = false;
        hasRuntimeError = false;
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() +
            "\n[line " + error.token.line + "]");
        hasRuntimeError = true;
    }

    private static void report(int line, String where, String message) {
        System.err.println(
            "[line " + line + "] Error " + where + ": " + message);
        hasError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, "at end", message);
        } else {
            report(token.line, "at '" + token.lexeme + "'", message);
        }
    }
}
