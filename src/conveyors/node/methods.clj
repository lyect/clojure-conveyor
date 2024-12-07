(ns conveyors.node.methods
  (:require [conveyors.utils          :as utils]
            [conveyors.node.keywords  :as node-keywords]
            [conveyors.node.hierarchy :as node-hierarchy]))


(defn node-type-defined?
  [type-keyword]
  (contains? @node-hierarchy/tree type-keyword))


(defn- get-node-property
  [node property]
  {:pre [(let [node-type (node node-keywords/T)]
           (and
            (some? node-type)
            (node-type-defined? node-type)))]}
  ((node-hierarchy/tree (node node-keywords/T)) property))


(defn get-node-type    [node] (get-node-property node node-keywords/T))
(defn get-node-super   [node] (get-node-property node node-keywords/super))
(defn get-node-inputs  [node] (get-node-property node node-keywords/inputs))
(defn get-node-outputs [node] (get-node-property node node-keywords/outputs))
(defn get-node-func    [node] (get-node-property node node-keywords/func))
(defn get-node-fields  [node] (get-node-property node node-keywords/fields))


; Node's fields
(defn get-node-field
  [node field-keyword]
  {:pre [(let [node-type (node node-keywords/T)]
           (and
            (some? node-type)
            (node-type-defined? node-type)))]}
  (node field-keyword))

; Constructor
(defn create
  [node-type-keyword & fields]
  {:pre [(node-type-defined? node-type-keyword)]}
  (let [fields-map (apply hash-map fields)
        node       (ref {})]
    (dosync (alter node #(assoc % node-keywords/T node-type-keyword)))
    (if (utils/lists-equal? (keys fields-map) (get-node-fields node))
      (do
        (doseq [fields-map-entry fields-map]
          (dosync (alter node #(assoc % (first fields-map-entry) (second fields-map-entry)))))
        node)
      (do
        (println (str "Passed field list is not equal to field list of " node-type-keyword))
        nil))))

; Abstract methods
(defn execute
  [node & params]
  (apply (get-node-func node) params))
