(ns infixxer.core)

(def ops
  {"u!" {:precedence 13 :alias `not}
   "u+" {:precedence 13}
   "u-" {:precedence 13}
   "/"  {:precedence 12}
   "*"  {:precedence 12}
   "**" {:precedence 12 :alias `Math/pow}
   "%"  {:precedence 12 :alias `mod}
   "+"  {:precedence 11}
   "-"  {:precedence 11}
   "<"  {:precedence 9}
   "<=" {:precedence 9}
   ">"  {:precedence 9}
   ">=" {:precedence 9}
   "==" {:precedence 8 :alias `=}
   "!=" {:precedence 8 :alias `not=}
   "&"  {:precedence 7 :alias `bit-and}
   "|"  {:precedence 5 :alias `bit-or}
   "&&" {:precedence 4 :alias `and}
   "||" {:precedence 3 :alias `or}
   })

(defn remove-at [v i]
  (let [len (count v)]
    (if (or (< i 0) (>= i len))
      v
      (into [] (concat (subvec v 0 i)
                       (subvec v (inc i) len))))))

(defn group-with-next-at [v i]
  (let [sub (subvec v i (+ 2 i))]
    (-> v
        (remove-at (inc i))
        (assoc i (apply list sub)))))

(defn tokenize [el]
  (cond
    (contains? ops (str el)) :op
    (contains? ops (str "u" el)) :op
    :else :other))

(defn add-unary-parentheses [expr]
  (let [tokens (map tokenize expr)
        padded (conj tokens :op)
        triplets (partition 3 1 padded)
        op-op-other? (fn [i el] (if (= el '(:op :op :other)) i))
        indices (keep-indexed op-op-other? triplets)]
    (if (empty? indices)
      {:expr expr :changed false}
      (loop [expr (vec expr)
             indices (reverse indices)]
        (if (empty? indices)
          {:expr (apply list expr) :changed true}
          (let [i (first indices)
                indices (rest indices)
                expr (group-with-next-at expr i)]
            (recur expr indices)))))))

(defn add-all-unary-parentheses [expr]
  (loop [expr expr]
    (let [{:keys [expr changed]} (add-unary-parentheses expr)]
      (if changed
        (recur expr)
        expr))))

(defn choose-op [candidates]
  (let [table (into [] (map-indexed
                         (fn [i op]
                           {:op op
                            :index i
                            :precedence (-> ops
                                            (get (str op) {})
                                            (:precedence 0))})
                         candidates))
        max-precedence (apply max (map :precedence table))]
    (first (filter
             #(= max-precedence (:precedence %))
             table))))

(defn add-parentheses [expr]
  (if (= 'fn* (first expr))
    ; just take the body of the anonymous function
    ; (fn* [] (...)) => (...)
    {:expr (nth expr 2) :changed false}
    (let [triplets (map vec (partition 3 2 expr))
          n (count triplets)]
      (if (< n 2)
        {:expr expr :changed false}
        (let [candidates (map second triplets)
              idx (:index (choose-op candidates))
              parts (map-indexed
                     (fn [i triplet]
                       (cond
                         (< i idx) (subvec triplet 0 2)   ; (a b _)
                         (= i idx) [(apply list triplet)] ; surround with (...)
                         (> i idx) (subvec triplet 1 3))) ; (_ b c)
                     triplets)]
          {:expr (apply concat parts) :changed true})))))

(defn add-all-parentheses [expr]
  (loop [expr expr]
    (let [{:keys [expr changed]} (add-parentheses expr)]
      (if changed
        (recur expr)
        expr))))

(defn process [expr]
  (if (seq? expr)
    (->> expr
         (map process)
         (add-all-unary-parentheses)
         (add-all-parentheses))
    expr))

(defn reorder [expr]
  (let [[x & _] expr]
    (if (= 'fn* x)
      (nth expr 2) ; just the body
      (condp = (count expr)
        1 (let [[x] expr]
            (if (seq? x) (reorder x) x))
        2 (let [[op x] expr
                op (-> ops
                       (get (str "u" op) {})
                       (:alias op))
                x (if (seq? x) (reorder x) x)]
            (list op x))
        3 (let [[x op y] expr
                op (-> ops
                       (get (str op) {})
                       (:alias op))
                x (if (seq? x) (reorder x) x)
                y (if (seq? y) (reorder y) y)]
            (list op x y))
        (throw (new Exception (str "wrong number of elements: " expr)))))))

(defn convert [expr]
  (-> expr
      (process)
      (reorder)))

(defmacro $= [& expr]
  (convert expr))