# infixxer

Infix to prefix conversion library for Clojure

## Installing

[![Clojars Project](https://img.shields.io/clojars/v/infixxer.svg)](https://clojars.org/infixxer)

## Usage

```
(require '[infixxer.core :refer [$= convert]])
```

### Basic Usage
```
($= 2 + 2)
=> 4

($= 2 + 3 * 5)
=> 17

(let [b 3 c 5]
  ($= 2 + b * c == 17))
=> true

(let [x1 3 y1 3 x2 6 y2 7]
  (Math/sqrt ($= (x1 - x2) ** 2 + (y1 - y2) ** 2)))
=> 5.0

(let [[f1 f2 f3 f4] [1 2 4 8]]
  ($= f1 | f2 | f3 | f4))
=> 15
```

You can use unary operators +, -, !

```
(let [a 2 b -2]
  ($= a == - b))
=> true

(let [a 3 b 5 c false]
  ($= ! (3 > 5) == true))
=> true
```

Note that there should be a space between unary operators and the operand (if the operand is a variable)

```
(let [a 2 b -2]
  ($= a == -b))
Syntax error compiling at (/private/var/folders/7l/t0szyzv57vq_8wjhh3k864nh0000gn/T/form-init14867636270497591582.clj:2:3).
Unable to resolve symbol: -b in this context
```

### Advanced Usage

Clojure calls inside (code inside #(...) not converted)

```

($= 100 + #(rand))
=> 100.56273391390084 (your output will vary a bit due to different random seed)

(let [a 1 b 2
      square (fn [x] (* x x))]
  ($= a + #(square b)))
=> 5

(let [a 1 b 2 c (fn [] 3)
      f (fn [x y] (+ x y))]
  ($= (a + #(f b (c))) / 2))
=> 3
```

Use (convert '(...)) to see the expansion

```
(convert '(a + b))
=> (+ a b)

(convert '(a + b * 2))
=> (+ a (* b 2))

(convert '((a + #(f b c)) / 2))
=> (/ (+ a (f b c)) 2)

(convert '(- a + b / #(rand) == ! c))
=> (clojure.core/= (+ (- a) (/ b (rand))) (clojure.core/not c))

```

## Supported operators

Note: "u" before unary operator names is just to prevent naming collision in the map. 

```
["u+" {:precedence 13}]
["u-" {:precedence 13}]
["u!" {:precedence 13, :alias clojure.core/not}]
["**" {:precedence 12, :alias java.lang.Math/pow}]
["*" {:precedence 12}]
["%" {:precedence 12, :alias clojure.core/mod}]
["/" {:precedence 12}]
["-" {:precedence 11}]
["+" {:precedence 11}]
["<=" {:precedence 9}]
[">=" {:precedence 9}]
["<" {:precedence 9}]
[">" {:precedence 9}]
["!=" {:precedence 8, :alias clojure.core/not=}]
["==" {:precedence 8, :alias clojure.core/=}]
["&" {:precedence 7, :alias clojure.core/bit-and}]
["|" {:precedence 5, :alias clojure.core/bit-or}]
["&&" {:precedence 4, :alias clojure.core/and}]
["||" {:precedence 3, :alias clojure.core/or}]
```

## License

MIT License

Copyright (c) 2020 Kadir Malak

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
