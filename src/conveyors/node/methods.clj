(ns conveyors.node.methods
  (:require [clojure.set               :as cljset]
            [conveyors.node.exceptions :as node-exceptions]
            [conveyors.node.hierarchy  :as node-hierarchy]
            [conveyors.node.properties :as node-properties]))


(defn node-type-defined?
  [type-keyword]
  (contains? @node-hierarchy/tree type-keyword))


(defn- get-node-property
  [node property]
  {:pre [(let [node-type (node node-properties/T)]
           (and
            (some? node-type)
            (node-type-defined? node-type)))]}
  ((node-hierarchy/tree (node node-properties/T)) property))


(defn get-node-type    [node] (get-node-property node node-properties/T))
(defn get-node-super   [node] (get-node-property node node-properties/super))
(defn get-node-inputs  [node] (get-node-property node node-properties/inputs))
(defn get-node-outputs [node] (get-node-property node node-properties/outputs))
(defn get-node-func    [node] (get-node-property node node-properties/func))
(defn get-node-fields  [node] (get-node-property node node-properties/fields))


; Node's fields
(defn get-node-field
  [node field-keyword]
  {:pre [(let [node-type (node node-properties/T)]
           (and
            (some? node-type)
            (node-type-defined? node-type)))]}
  (node field-keyword))

; Constructor
(defn create
  [node-type-keyword & fields]
  {:pre [(node-type-defined? node-type-keyword)]}
  (let [fields-map           (apply hash-map fields)
        node-fields          (keys fields-map)
        node-type-fields     ((node-hierarchy/tree node-type-keyword) node-properties/fields)
        node-fields-set      (set node-fields)
        node-type-fields-set (set node-type-fields)
        node                 (ref {})]
    (when-not (<= (count (take-nth 2 fields)) (count node-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/duplicating-fields
                                        (str "Tried to create " node-type-keyword " with duplicated fields"))))
    (when-not (empty? (cljset/difference node-type-fields-set node-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/missing-fields
                                        (str "Tried to create " node-type-keyword " with missing fields"))))
    (when-not (empty? (cljset/difference node-fields-set node-type-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/excess-fields
                                        (str "Tried to create " node-type-keyword " with excess fields"))))
    (doseq [fields-map-entry fields-map]
      (dosync (alter node #(assoc % (first fields-map-entry) (second fields-map-entry)))))
    (dosync (alter node #(assoc % node-properties/T node-type-keyword)))))

; Abstract methods
(defn execute
  [node & params]
  (apply (get-node-func node) params))
