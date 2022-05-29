package com.brandonaguirre.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    // use to handle error reporting
    static ErrorReporter errorReporter = new ErrorReporter();
    private static final Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(1);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        // had an error handle exit
        if (errorReporter.hadError()) System.exit(65);
        if (errorReporter.hadRuntimeError()) System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        while (true) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null) break;
            try {
                run(line);
            } catch (RuntimeException error) {
                // nothing I guess?
            }
            // don't kill entire session because of an error
            errorReporter.resetError();
        }
    }

    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();

        // for (Token token: tokens) {
        //     System.out.print( "{" + token.toString() + "} ");
        // }
        // System.out.println("");

        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (errorReporter.hadError()) return;

        // System.out.println(new AstPrinter().print(expr));
        interpreter.interpret(statements);
    }
}
