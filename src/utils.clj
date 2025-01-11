(ns utils)

(defn in-list? [lst elem]
  (some #(= % elem) lst))

(defn lists-equal? [l1 l2]
  (and (.containsAll l1 l2) (.containsAll l2 l1)))

(defn zip [coll1 coll2]
  (map vector coll1 coll2))

(defn ref? [obj]
  (instance? clojure.lang.Ref obj))

(defn not-ref? [obj]
  (not (ref? obj)))
