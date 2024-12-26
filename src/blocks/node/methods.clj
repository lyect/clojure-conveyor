(ns blocks.node.methods
  (:require [clojure.set                         :as cljset]
            [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.exceptions              :as node-exceptions]
            [blocks.node.hierarchy               :as node-hierarchy]
            [blocks.node.properties              :as node-properties]
            [blocks.node.types                   :as node-types]
            [utils]))


;; +-----------------------------+
;; |                             |
;; |   NODE RELATED PREDICATES   |
;; |                             |
;; +-----------------------------+

(def node?
  (memoize
   (fn [obj-ref]
     (and (some? @obj-ref)
          (map? @obj-ref)
          (obj-ref base-node-fields/type-name)
          (node-types/defined? (obj-ref base-node-fields/type-name))))))

;; +----------------------------------------+
;; |                                        |
;; |   NODE TYPE RELATED PROPERTY GETTERS   |
;; |                                        |
;; +----------------------------------------+

;; No need to check whether node has the property or not. get-node-property is a private function,
;; hence it is used only for specific properties
(defn- get-node-property
  [node-ref property]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-node-property node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node: " @node-ref))))
  ((node-hierarchy/tree (node-ref base-node-fields/type-name)) property))

;; No need to check whether "node" is a correct node or not
;; It will be done inside "get-node-property"
(def get-node-type-name       (memoize (fn [node-ref] (get-node-property node-ref node-properties/type-name))))
(def get-node-super-name      (memoize (fn [node-ref] (get-node-property node-ref node-properties/super-name))))
(def get-node-inputs          (memoize (fn [node-ref] (get-node-property node-ref node-properties/inputs))))
(def get-node-outputs         (memoize (fn [node-ref] (get-node-property node-ref node-properties/outputs))))
(def get-node-ready-validator (memoize (fn [node-ref] (get-node-property node-ref node-properties/ready-validator))))
(def get-node-function        (memoize (fn [node-ref] (get-node-property node-ref node-properties/function))))
(def get-node-fields          (memoize (fn [node-ref] (remove (set base-node-fields/fields-list) (get-node-property node-ref node-properties/fields)))))

;; +---------------------------------+
;; |                                 |
;; |   NODE INSTANCE FIELDS GETTER   |
;; |                                 |
;; +---------------------------------+

(def get-node-name
  (memoize
   (fn [node-ref]
     (when-not (node? node-ref)
       (throw (node-exceptions/construct node-exceptions/get-node-name node-exceptions/not-node
                                         (str "\"" node-ref "\" is not a node"))))
     (node-ref base-node-fields/node-name))))

(defn get-node-field
  [node-ref field-name]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-node-field node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (let [protected-node (apply dissoc @node-ref base-node-fields/fields-list)]
    (when-not (protected-node field-name)
      (throw (node-exceptions/construct node-exceptions/get-node-field node-exceptions/unknown-field
                                        (str "\"" node-ref "\" has no field named \"" field-name "\""))))
       ; Base class fields protection
    (protected-node field-name)))

;; +----------------------+
;; |                      |
;; |   NODE CONSTRUCTOR   |
;; |                      |
;; +----------------------+

(defn create
  [node-type-name node-name & fields]
  (when-not (node-types/declared? node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undeclared
                                         (str "Type named \"" node-type-name "\" is undeclared"))))
  (when (node-types/abstract? node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/abstract-creation
                                      (str "Unable to instantiate node with abstract type " node-type-name))))
  (when-not (node-types/defined? node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undefined
                                         (str "Type named \"" node-type-name "\" is undefined"))))
  (let [fields-map           (apply hash-map fields)
        node-fields          (keys fields-map)
        node-fields-set      (set node-fields)
        node-type-fields     (remove (set base-node-fields/fields-list) ((node-hierarchy/tree node-type-name) node-properties/fields))
        node-type-fields-set (set node-type-fields)
        node-ref             (ref {})]
    (when (> (count (take-nth 2 fields)) (count node-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/duplicating-fields
                                        (str "Tried to create " node-type-name " with duplicated fields"))))
    (when-not (empty? (cljset/difference node-type-fields-set node-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/missing-fields
                                        (str "Tried to create " node-type-name " with missing fields"))))
    (when-not (empty? (cljset/difference node-fields-set node-type-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/excess-fields
                                        (str "Tried to create " node-type-name " with excess fields"))))
    (doseq [fields-map-entry fields-map]
      (alter node-ref #(assoc % (first fields-map-entry) (second fields-map-entry))))
    (alter node-ref #(assoc % base-node-fields/node-name             node-name))
    (alter node-ref #(assoc % base-node-fields/type-name             node-type-name))
    (alter node-ref #(assoc % base-node-fields/input-buffers         (repeatedly (count (get-node-inputs  node-ref)) (fn [] (ref [])))))
    (alter node-ref #(assoc % base-node-fields/input-buffers-amounts (repeat     (count (get-node-inputs node-ref)) 1)))
    (alter node-ref #(assoc % base-node-fields/output-buffers        (repeatedly (count (get-node-outputs node-ref)) (fn [] (ref [])))))
    
    (when (fields-map base-node-fields/input-buffers-amounts)
      (alter node-ref #(assoc % base-node-fields/input-buffers-amounts (reduce
                                                                        (fn [input-buffers-amounts [input-index input-amount]]
                                                                          (assoc input-buffers-amounts input-index input-amount))
                                                                        (node-ref base-node-fields/input-buffers-amounts)
                                                                        (fields-map base-node-fields/input-buffers-amounts)))))
    node-ref))

;; +------------------+
;; |                  |
;; |   NODE METHODS   |
;; |                  |
;; +------------------+

(def ^:private get-input-buffer-ref
  (memoize
   (fn [node-ref input-index]
     (nth (node-ref base-node-fields/input-buffers) input-index))))

(def ^:private get-output-buffer-ref
  (memoize
   (fn [node-ref output-index]
     (nth (node-ref base-node-fields/output-buffers) output-index))))

(defn store
  [node-ref input-index value]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/store node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (alter (get-input-buffer-ref node-ref input-index) #(conj % value)))

(defn flush-output
  [node-ref output-index]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/flush-output node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (let [output-buffer-ref (get-output-buffer-ref node-ref output-index)
        output-buffer @output-buffer-ref]
    (ref-set output-buffer-ref [])
    output-buffer))

(defn execute
  [node-ref]
  (while ((get-node-ready-validator node-ref) node-ref)
    ((get-node-function node-ref) node-ref)))
