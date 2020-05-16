# infixxer

Infix to prefix conversion library for Clojure

## Installing

- Leiningen/Boot

```
[infixxer "0.1.0"]
```

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

Copyright © 2020 Kadir Malak

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
