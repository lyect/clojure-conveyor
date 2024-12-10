(ns blocks.utils)

(defn in-list? [lst elem]
  (some #(= % elem) lst))

(defn lists-equal? [l1 l2]
  (and (.containsAll l1 l2) (.containsAll l2 l1)))