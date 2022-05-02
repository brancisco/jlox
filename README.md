# An Exercise In Interpreters

I'm following along in the book "Crating Interpreters" by Robert Nystrom (2015-2020) to build a better understanding of how interpreters and compilers work.

In this repo I'll be following along with some modifications for the first interpreter "jlox" which is written in Java.

## Build & Run

```
javac -d classes src/com/brandonaguirre/lox/*.java
java -cp classes com.brandonaguirre.lox.Lox
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

## Parsing

TODO
