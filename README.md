# An Exercise In Interpreters

I'm following along in the book "Crafting Interpreters" by Robert Nystrom (2015-2020) to build a better understanding of how interpreters and compilers work.

In this repo I'll be following along with some modifications for the first interpreter "jlox" which is written in Java. If you want a speed summary of the steps to build an interpreter, do read the rest of the readme!

Again the main point of this repo is just a learning exercise and a place for me to come back to for some cliff notes of the book. If you're reading this, please feel free to continue, but definitely check out the book "Crafting Interpreters" it is so great!

## Build & Run

```sh
# build and run main lox interpreter
javac -d classes src/com/brandonaguirre/lox/*.java
java -cp classes com.brandonaguirre.lox.Lox

# build and run ast generation
javac -d classes/ ./src/com/brandonaguirre/tool/*.java
java -cp classes com.brandonaguirre.tool.GenerateAst ./src/com/brandonaguirre/lox/
```

## Scanning (Lexing)

Our first step in this journey is *scanning*. During the process of scanning we'll create *tokens* which will be then used in the next step (parsing.)

The lox language will support the following:

### Data Types:

- Booleans
    - `true`
    - `false`
- Numbers
    - `1234`
    - `12.34`
- Strings
    - `"I am a string"`
    - `""`
    - `"123"`
- Nil (similar to Null)
    - `nil`

### Expressions:

- Arithmetic
    - `add + me`
    - `subtract - me`
    - `multiply * me`
    - `divide / me`
    - `-negateMe`
- Comparison and equality
    - `less < than`
    - `lessThan <= orEqual`
    - `greater > than`
    - `greaterThan >= orEqual`
    - `1 == 2`
    - `"cat" != "dog"`
- Logical operators
    - `!true`
    - `true and false`
    - `true or flase`
- Precedence and grouping
    - `(min + max) / 2`

> Note: the `and` and `or` are expressions which will evaluate to whichever makes the statement true or the last.

### Statements:

- `print "hello world";`
- `"hello world;`
```
"hello world";
{
    print "first thing";
    print "second thing";
}
```

### Variables

- `var iAmVariable = "hello"`
- `var iAmNil;`

### Control Flow

```
if (condition) {
    print "yes";
} else {
    print "no";
}

a = 1;
while (a) {
    print a;
    a = a + 1;
}

for (var a = 1; a < 10; a = a + 1) {
    print a;
}
```

### Functions

- function call
    - `makeBreakfast(bacon, eggs, toast);`
    - `makeBreakfast();`

```
fun printSum (a, b) {
    print a + b;
}

fun returnSum(a, b) {
    return a + b;
}
```

> Note: If function reaches end of block return Nil

### Classes

```java
class Breakfast {
    cook() {
        print "Eggs a-fryin'!";
    }
    serve(who) {
        print "Enjoy your breakfast, " + who + ".";
    }
}

var breakfast = Breakfast();
breakfast.meat = "sausage";
breakfast.bread = "sourdough";


// ---------

class Breakfast {
    init(meat, bread) {
        this.meat = meat;
        this.bread = bread;
    }
}

var baconAndToast = Breakfast("bacon", "toast");
baconAndToast.serve("Dear Brandon");
// "Enjoy your bacon and toast, Dear Brandon."

class Brunch < Breakfast {
    init(meat, bread, drink) {
        super.init(meat, bread);
        this.drink = drink;
    }
}
```

### Some Interesting Things To Note

**Lexical Grammar**

The rules of how you group characters together is a languages *lexical grammar*. Lox's lexical grammar is also a *regular grammar*. This means that Lox's language's rules for grouping characters together are simple enough to be done with *regular expressions*.

> An example of a non regular language would be Python. In Python we would need to count and keep track of what level of indentation we are in. These rules are more complex than a regular language.

**Lookahead & Maximal Munch**

Lexemes that are longer than one character require some type of lookahead. **Lookahead** Allows you to *look ahead* `n` characters ahead without consuming those characters. Generally, most languages only need one (the current unconsumed char) or two (the next unconsumed char) characters of lookahead. Typically the shorter the lookahead needed, the faster the scanner runs.

What happens when two lexemes start off the same. The lexical grammar will match both of these lexemes, so how do we choose which rule to apply. The principle of **Maximal Munch** tells us to use whichever rule matches the most characters.

As an example we can look at the string `"<="`. We could either parse this as two separate lexemes: `<` and `=`, however, that syntax wouldn't make sense in our language. The user means to use the less than or equal operator. So we opt to use the longer possible match which would be `<=`.

**Error Reporting**

As we scan through the source code, we should report as many errors as we can find. This avoids the user from trying to solve one error at a time. We'll keep a flag of whether we've seen an error, and we won't try to execute the code.

**Reserved Words & Identifiers**

These are essentially read in the same so we read them with the same lexical grammar rule. To identify the reserved words, its as easy as looking in a table or hash map to see if the lexeme is in our bank of reserved words. If it's not than its simply an identifier.

## Context Free Grammar & How To Represent An Abstract Syntax Tree

We have an idea of what our language will look like and what features will grace its landscape. We now need to formalize this. How do we determine if a sequence of tokens from our scanner is valid or not?

We'll create a context free grammar that will describe our language more formally. It will be the set of canonical rules we must obey to form valid sequences of our language.

> A context-free grammar is "stronger" than a regular language. Regular languages can *repeat* but cannot *count*. The counting part here refers to the fact that in a context-free grammar we can have a recursive non-terminal surrounded by productions on both sides. We need to keep track (count) of how many of those productions will be on the sides.

Example of counting:
```
The (context-free) grammar:
S -> aSb | A | 𝜀
A -> cA | 𝜀

Some valid derivations:
c
ab
aabb
acb
acccb
aaacbbb
...

Note how we get to some derivation like aaacbbb:
1. aS(b)
2. aaS(b)(b)
3. aaaS(b)(b)(b)
4. aaac(b)(b)(b)
5. aaacb(b)(b)
6. aaacbb(b)
7. aaacbbb

We needed to keep track of those "b"s that were accumulating. We can only do this in something as strong as (or stronger than) a context-free grammar.

The (regular) grammar:
S -> aS | bS | cS | 𝜀

Some valid derivations:
a
ab
acb
cab
aaacbbb
...

Note the derivations can be the same, however, see how we get to the derivation aaacbbb:
1. aS
2. aaS
3. aaaS
4. aaacS
5. aaacbS
6. aaacbbS
7. aaacbbb

We don't keep track of anything here, we just recursively repeat.
```

### Our BNF (Backus-Naur form) Grammar Representation

> For now we're only defining a subset of our language.

```
# Lowest precedence
expression -> equality ;
equality   -> comparison ( ( "==" | "!=" ) comparison )* ;
comparison -> term ( ( "<" | "<=" | ">" | ">=" ) term )* ;
term       -> factor ( ( "+" | "-" ) factor)* ;
factor     -> unary ( ( "/" | "*" ) unary )* ;
unary      -> ( "!" | "-" ) unary | primary ;
primary    -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
# Highest precedence
```

> This grammar isn't entirely correct. We can generate some corner cases we'll have to account for in our code. An example we could generate: `"hello" / "hello"`.

The above definition does eliminate ambiguity due to precedence. We'll take care of ambiguity due to associativity (the order the same operation is evaluated; left -> right | right -> left) in our code.

> What is the difference between a parse tree and an abstract syntax tree? In a parse tree, every single *production* (e.g., expression -> equality ; ) becomes a node in the parse tree. In an AST, some nodes may be elided. The AST is a more compact form that only needs to keep track of productions that are necessary for later phases.

In an object oriented language, we can use classes to define our AST. Note that we don't want to keep any behavior like evaluating or processing nodes in our classes. We only want to create a data structure to represent our AST. We'll use the **visitor pattern** to add methods for processing our tree at later phases.

Example of representing our AST (note we would need to add support for the visitor pattern):
```java
abstract class Expr {
    class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    class Unary extends Expr {
        final Expr right;
        final Token operator;

        Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
    }

    // ... etc
}
```

## Parsing Expressions

Okay things are starting to get exciting. We scanned our source code and found the tokens using our lexical grammar. We decided on a way to represent our AST and expressions, so now we need to parse the expressions hidden in our tokens.

> Recommended read: "Compliers: Principles, Techniques, and Tools"

In the last section we talked about our grammar, it's precedence, and associativity. That grammar was written to be **right-recursive** intentionally so that we wouldn't have any issues with our parsing method of choice - Recursive Descent Parsing. Using left recursion in our grammar would cause issues because a function would call itself until a stack error occurs instead of moving on to other functions in the grammar. Perhaps an example is in order:

```java
// consider the following left-recursive production
// factor -> factor ( "/" | "*" ) unary | unary ;

// the recursive descent function for the production above
public Expr factor() {
    Expr left = factor();
    // I can already stop here.. as you can see we'll just
    // recurse ad nauseam until we blow the stack
}

// now consider the following right-recursive production
// factor -> unary ( ( "/" | "*" ) unary )* ;

// the recursive descent function for the production above
public Expr factor() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
}
```

Although both of these production are left associative and valid, the rule doesn't quite fit how we'd like to code it. Choosing a grammar that fits our model is easier to follow along while coding.

### Recursive Descent Parsing

Recursive descent is a simple (but powerful) way to build a parser. Recursive descent is considered a top down parser. We walk from the outermost grammar down to the innermost sub-expressions.

```
Grammar  |Precedence
Top      |     Lower
-        |         -
| ---------------- |
| Equality         |
| ---------------- |
| Comparison       |
| ---------------- |
| Addition         |
| ---------------- |
| Multiplication   |
| ---------------- |
| Unary            |
| ---------------- |
-       |          -
Bottom  |    Higher
```

Probably the way to best understand what recursive descent is doing is to think of it as a literal translation of the grammar's rules into imperative code.

Let's go through a couple examples of translating some grammar rules into recursive code. Take a look at the toy grammar below.

```
expression -> add
add        -> multiply ( "+" multiply )*
multiply   -> number ( "*" number )*
number     -> ( [1-9][0-9]* | [0-9] )
```

And lets walk through a couple examples to make sure our grammar is what we want. Oh and what is it we want by the way? We want:

- addition
- multiplication
- multiplication takes precedence over addition
- we can have as many additions or multiplications as we want

Okay here are some examples:

```
1 + 1
-----
expression
add
multiply ( "+" multiply )*
number ( "+" multiply )*
1 ( "+" multiply )*
1 + multiply
1 + ( number ( "*" number )* )
1 + number
1 + 1

1 + 2 * 3
---------
expression
add
multiply ( "+" multiply )*
number ( "+" multiply )*
1 ( "+" multiply )*
1 + multiply
1 + ( number ( "*" number )* )
1 + ( 2 ( "*" number )* )
1 + ( 2 * number )
1 + 2 * 3
```

So we see we can perform some additions and multiplications, but what about precedence? Does the `1 + 2 * 3` evaluate to be? `1 + (2 * 3) = 7` or `(1 + 2) * 3 = 9`. Well we said earlier we want multiplication to take precedence.

In one sense, we've answered this question with our grammar. Rules with higher precedence are lower in the grammar. This means multiplication will have the higher precedence. We're still missing how this precedence is taken care of with recursive descent. Let's take a look at an example:

```java
public class Parser {
    private Expr expression() {
        return add();
    }

    private Expr add() {
        Expr expr = multiply();

        while (match(PLUS)) {
            Token operator = previous();
            Expr right = multiply();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr multiply() {
        Expr expr = number();

        while (match(STAR)) {
            Token operator = previous();
            Expr right = number();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr number() {
        if (match(NUMBER)) {
            return new Expr.Literal(previous().literal);
        }
        throw error(peek(), "Expect number.");
    }
}
```

With the above parser, its easier to see how our precedence works out. In the `add()` function we consume `PLUS` tokens and nest multiplication expressions inside of those addition expressions. When we evaluate, we'll need to evaluate those most inner expressions first! Boom precedence taken care of.

Oh yea, what about **associativity**? That detail is taken care of in our implementation above. We nest expressions in our while loop on the left side of the `Binary` expressions. So `1 + 2 + 3` becomes `Binary(Binary(1, "+", 2), "+", 3)`. Then naturally, viewing as a tree, we would evaluate the leaf nodes first, and evaluate `1 + 2` then evaluate `3 + 3` finally returning `6`.

Finally, here is some other syntax in our grammar we might run into and a mapping of how we would represent that in code

```
Terminal    -> consume a token
Nonterminal -> call to rule's function
|           -> if/switch statement
* or +      -> while/for loop
?           -> if statement
```

### Handling Syntax Errors

Wow, a syntax error. Now what? We need to do a couple things here:

- Detect and report error
- NO crashing and NO hanging

and in addition:

- Be fast
- Report as many errors as there are
- minimize *cascaded* errors

In order to achieve some of these things, when we run into an error, we want to be able to continue to parse to find as many errors as possible. This is called **error recovery**. Author Robert Nystrom suggests we handle errors using **panic mode** which is apparently a tried and true method.

> What other error recovery methods are there? See **statement mode**.

When we run into an error we need to get the parsers state and sequence of forthcoming tokens aligned again. This is called **synchronization**. The general method to do this is to pick some rule in our grammar that will mark the *synchronization point*. The easiest thing to do is use the end or beginning of a statement. The semicolon!
In order to do this we need to jump out of whatever rule we were in the middle of parsing and start searching for our synchronization point. To jump out in our recursive descent parser in Java, we'll throw an error to clear the call stack and catch the error high up where appropriate.

## Evaluating Expressions

TODO
