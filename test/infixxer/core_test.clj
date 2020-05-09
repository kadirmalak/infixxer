(ns infixxer.core-test
  (:require [clojure.test :refer :all]
            [infixxer.core :refer :all]))

(deftest test-remove-at
  (are [v i expected] (= expected (remove-at v i))
       [1 2 3] 0 ,,, [2 3]
       [1 2 3] 1 ,,, [1 3]
       [1 2 3] 2 ,,, [1 2]

       [1 2 3] -1 ,,, [1 2 3]
       [1 2 3] 10 ,,, [1 2 3]

       [] -1 ,,, []
       [] 0 ,,, []
       [] 1 ,,, []
       ))

(deftest test-group-with-next-at
  (are [v i expected] (= expected (group-with-next-at v i))
       [1 2 3] 0 ,,, ['(1 2) 3]
       [1 2 3] 1 ,,, [1 '(2 3)]
       ))

(deftest test-tokenize
  (is (= [:op :other :op :other :op :other] (map tokenize '(! a / 3 - 5)))))

(deftest test-add-unary-parentheses
  (testing "should not change"
    (are [input]
         (let [{:keys [expr changed]} (add-unary-parentheses input)]
           (and (not changed)
                (= input expr)))
         '(a)
         '(+ a)
         '(a + b)
         '(a + b + c)
         '((+ - a))
         '((+ - a) + (+ - b))
         '((+ - a) + (+ - b) + (+ - c))
         ))
  (testing "should change"
    (are [input expected]
         (let [{:keys [expr changed]} (add-unary-parentheses input)]
           (and changed
                (= expected expr)))
         '(+ a - + b) ,,, '((+ a) - (+ b))
         '(+ a - + b - + c) ,,, '((+ a) - (+ b) - (+ c))
         '(+ + + a - - - b + + + c) ,,, '(+ + (+ a) - - (- b) + + (+ c))
         '(+ + (+ a) - - (- b) + + (+ c)) ,,, '(+ (+ (+ a)) - (- (- b)) + (+ (+ c)))
         ))
  )

(deftest test-add-all-unary-parentheses
  (are [input expected] (= expected (add-all-unary-parentheses input))
       '(+ + + a - - - b + + + c) ,,, '((+ (+ (+ a))) - (- (- b)) + (+ (+ c)))
       ))

(deftest test-choose-op
  (are [ops expected]
       (let [{:keys [op index precedence]} (choose-op ops)]
         (= expected index))
       '(+ - / *) ,,, 2
       '(+ - * /) ,,, 2
       '(? ? ?) ,,, 0
       ))

(deftest test-add-binary-parentheses
  (testing "should not change"
    (are [input] (let [{:keys [expr changed]} (add-binary-parentheses input)]
                   (and (not changed)
                        (= input expr)))
         '(a)
         '(a + b)
         '((a + b - c))
         '((a + b - c) * (d + e - f))
         ))
  (testing "should change"
    (are [input expected] (let [{:keys [expr changed]} (add-binary-parentheses input)]
                   (and changed
                        (= expected expr)))
         '(a + b + c) ,,, '((a + b) + c)
         '(a * b + c) ,,, '((a * b) + c)
         '(a + b * c) ,,, '(a + (b * c))
         '(a + b + c + d) ,,, '((a + b) + c + d)
         '(a * b + c + d) ,,, '((a * b) + c + d)
         '(a + b * c + d) ,,, '(a + (b * c) + d)
         '(a + b + c * d) ,,, '(a + b + (c * d))
         '((a + b + c) + (d + e + f) + (g + h + i)) ,,, '(((a + b + c) + (d + e + f)) + (g + h + i))
         ))
  )

(deftest test-add-all-binary-parentheses
  (are [input expected] (= expected (add-all-binary-parentheses input))
       '(a + b + c + d) ,,, '(((a + b) + c) + d)
       '(a * b / c < d) ,,, '(((a * b) / c) < d)
       '(a < b + c / d) ,,, '(a < (b + (c / d)))
       '(a < b * c + d) ,,, '(a < ((b * c) + d))
       '(a + b * c < d) ,,, '((a + (b * c)) < d)
       '(a * b + c / d) ,,, '((a * b) + (c / d))
       ))

(deftest test-add-all-parentheses
  (are [input expected] (= expected (add-all-parentheses input))
       '(- a + (b / c ** d) * - (- c)) ,,, '((- a) + (((b / c) ** d) * (- (- c))))
       '(- a + #(f b) * c) ,,, '((- a) + ((fn* [] (f b)) * c))
       ))

(deftest test-reorder-and-replace
  (are [input expected] (= expected (reorder-and-replace input))
       '(a) ,,, 'a
       '(+ a) ,,, '(+ a)
       '(a + b) ,,, '(+ a b)

       '(a + (b + (c + d))) ,,, '(+ a (+ b (+ c d)))
       '(((a + b) + c) + d) ,,, '(+ (+ (+ a b) c) d)
       '(a < ((b * c) + d)) ,,, '(< a (+ (* b c) d))
       '((a + (b * c)) < d) ,,, '(< (+ a (* b c)) d)
       '((a * b) + (c / d)) ,,, '(+ (* a b) (/ c d))

       '(! a) ,,, '(clojure.core/not a)
       '(a % b) ,,, '(clojure.core/mod a b)
       '(a ** b) ,,, '(java.lang.Math/pow a b)

       '(a + (f)) ,,, '(+ a f)

       '(a + #(f)) ,,, '(+ a (f))
       '(a + #(f b)) ,,, '(+ a (f b))
       '(a + #(f b c)) ,,, '(+ a (f b c))
       '(a + #(f b c d)) ,,, '(+ a (f b c d))
       ))

(deftest test-convert
  (are [input expected] (= expected (convert input))
       '(a) ,,, 'a
       '((a)) ,,, 'a
       '(a + b) ,,, '(+ a b)
       '(- a + b / #(rand) == ! c) ,,, '(clojure.core/=
                                         (+ (- a) (/ b (rand)))
                                         (clojure.core/not c))
       '(#(f - 1 2) != #(g + 1 2)) ,,, '(clojure.core/not= (f - 1 2) (g + 1 2))
       '(a * #(rand 5) + b <= - c || d ** (e + x) != (f / 2) % g) ,,, '(clojure.core/or
                                                                        (<= (+ (* a (rand 5)) b) (- c))
                                                                        (clojure.core/not=
                                                                         (java.lang.Math/pow d (+ e x))
                                                                         (clojure.core/mod (/ f 2) g)))
       ))
