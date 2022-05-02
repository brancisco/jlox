package com.brandonaguirre.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import static com.brandonaguirre.lox.TokenType.*;

class Scanner {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int start = 0;
    private int line = 1;

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("fun", FUN);
        keywords.put("for", FOR);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    Scanner(String source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isEOF()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char ch = source.charAt(current);

        switch (ch) {
            case '(': addToken(LEFT_PAREN); break;
            case ')': addToken(RIGHT_PAREN); break;
            case '{': addToken(LEFT_BRACE); break;
            case '}': addToken(RIGHT_BRACE); break;
            case ',': addToken(COMMA); break;
            case '.': addToken(DOT); break;
            case '-': addToken(MINUS); break;
            case '+': addToken(PLUS); break;
            case ';': addToken(SEMICOLON); break;
            case '*': addToken(STAR); break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL: GREATER);
                break;
            case '/':
                // comment goes until end of the line
                if (match('/')) {
                    while (peek() != '\n' && !isEOF()) current ++;
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ': break;
            case '\r': break;
            case '\t': break;
            case '\n':
                line ++;
                break;
            case '"': scanString(); break;
            default:
                if (isDigit(ch)) {
                    scanNumber();
                } else if (isAlpha(ch)) {
                    scanIdentifier();;
                } else {
                    Lox.errorReporter.error(line, "Unexpected character.");
                }
                break;
        }
    }

    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') ||
               (ch >= 'A' && ch <= 'Z') ||
               (ch == '_');
    }

    private boolean isAlphaNumeric(char ch) {
        return isDigit(ch) || isAlpha(ch);
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();

        String lexeme = source.substring(start, current);
        TokenType type = keywords.get(lexeme);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void scanNumber() {
        while (isDigit(peek())) advance();

        // check for fractional part and consume '.' if found
        if (peek() == '.' && isDigit(peekNext())) advance();

        while (isDigit(peek())) advance();

        addToken(NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void scanString() {
        while (peek() != '"' && !isEOF()) {
            if (peek() == '\n') line ++;
            advance();
        }

        // if we never get a closing "
        if (isEOF()) {
            Lox.errorReporter.error(line, "Unterminated String.");
            return;
        }
        // consume the closing "
        advance();

        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private char peek() {
        if (isEOF()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isEOF()) return false;
        if (current != expected) return false;

        current ++;
        return true;
    }

    private boolean isEOF() {
        return current >= source.length();
    }

    private char advance() {
        current ++;
        return source.charAt(current - 1);
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

}
