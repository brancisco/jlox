# An Exercise In Interpreters

I'm following along in the book "Crafting Interpreters" by Robert Nystrom (2015-2020) to build a better understanding of how interpreters and compilers work.

In this repo I'll be following along with some modifications for the first interpreter "jlox" which is written in Java.

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

```
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

## Parsing

TODO
