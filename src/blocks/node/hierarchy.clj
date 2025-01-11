(ns blocks.node.hierarchy 
  (:require [blocks.node.exceptions :as node-exceptions]))


(def tree (ref {}))

(def ^:private nodes-labels (hash-set))

(defn label-reserved?
  [label]
  (some? (nodes-labels label)))

(defn reserve-label
  [label]
  (when (label-reserved? label)
    (node-exceptions/construct node-exceptions/reserve-label node-exceptions/label-reserved
                               (str "Label \"" label "\" is reserved")))
  (conj nodes-labels label))
