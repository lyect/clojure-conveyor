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

(defn node?
  "Predicate to check whether _obj_ is node or not"
  [obj-ref]
  (and (some? @obj-ref)
       (map? @obj-ref)
       (obj-ref base-node-fields/type-name)
       (node-types/defined? (obj-ref base-node-fields/type-name))
       (utils/lists-equal? (keys @obj-ref)
                           ((node-hierarchy/tree (obj-ref base-node-fields/type-name)) node-properties/fields))))

;; +----------------------------------------+
;; |                                        |
;; |   NODE TYPE RELATED PROPERTY GETTERS   |
;; |                                        |
;; +----------------------------------------+

;; No need to check whether node has the property or not. get-node-property is a private function,
;; hence it is used only for specific properties
(defn- get-node-property
  "Get _property_ of _node_"
  [node-ref property]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-node-property node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  ((node-hierarchy/tree (node-ref base-node-fields/type-name)) property))

;; No need to check whether "node" is a correct node or not
;; It will be done inside "get-node-property"
(defn get-node-type-name        [node-ref] (get-node-property node-ref node-properties/type-name))
(defn get-node-super-name       [node-ref] (get-node-property node-ref node-properties/super-name))
(defn get-node-inputs           [node-ref] (get-node-property node-ref node-properties/inputs))
(defn get-node-outputs          [node-ref] (get-node-property node-ref node-properties/outputs))
(defn get-node-function         [node-ref] (get-node-property node-ref node-properties/function))
(defn get-node-ready-validator  [node-ref] (get-node-property node-ref node-properties/ready-validator))
(defn get-node-inputs-validator [node-ref] (get-node-property node-ref node-properties/inputs-validator))
(defn get-node-fields           [node-ref] (remove (set base-node-fields/fields-list) (get-node-property node-ref node-properties/fields)))

;; +---------------------------------+
;; |                                 |
;; |   NODE INSTANCE FIELDS GETTER   |
;; |                                 |
;; +---------------------------------+

(defn get-node-name
  "Get name of _node_"
  [node-ref]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-node-name node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (node-ref base-node-fields/node-name))

(defn get-node-field
  "Get field's value of _node_ by _field-name_"
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
  "Create node typed as _node-type-name_ with name _node-name_, fills fields with _fields_.
   _fields_ must be a collection consisting of pairs such as (f1 v1 f2 v2 ... fn vn)"
  [node-type-name node-name & fields]
  (when-not (utils/in-list? node-types/types-list node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undeclared
                                         (str "Type named \"" node-type-name "\" is undeclared"))))
  (when-not (node-types/defined? node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undefined
                                         (str "Type named \"" node-type-name "\" is undefined"))))
  (when (= node-type-name node-types/NodeT)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/abstract-creation
                                         (str "Unable to instantiate abstract node"))))
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
    (alter node-ref #(assoc % base-node-fields/node-name      node-name))
    (alter node-ref #(assoc % base-node-fields/type-name      node-type-name))
    (alter node-ref #(assoc % base-node-fields/input-buffers  (repeat (count (get-node-inputs  node-ref)) (ref ()))))
    (alter node-ref #(assoc % base-node-fields/output-buffers (repeat (count (get-node-outputs node-ref)) (ref ()))))
    node-ref))

;; +------------------+
;; |                  |
;; |   NODE METHODS   |
;; |                  |
;; +------------------+

(defn- get-input-buffer-ref
  [node-ref input-index]
  (let [input-buffer-ref (get ((node-ref node-properties/fields) base-node-fields/input-buffers) input-index)]
    (when (nil? input-buffer-ref)
      (throw (node-exceptions/construct node-exceptions/store node-exceptions/no-buffer-under-index
                                        (str "No buffer in \"" node-ref "\" by index \"" input-index "\""))))
    input-buffer-ref))

(defn store
  [node-ref input-index value]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/store node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (alter (get-input-buffer-ref node-ref input-index) #(into % value)))

(defn execute
  [node-ref]
  (let [ready-validator  (get-node-ready-validator node-ref)
        inputs-validator (get-node-inputs-validator node-ref)]
    (while (ready-validator node-ref)
      (when-not (inputs-validator node-ref)
        (throw (node-exceptions/construct node-exceptions/execute node-exceptions/inputs-unvalidated
                                          (str "Inputs of \"" node-ref "\" are invalidated"))))
      ((get-node-function node-ref) node-ref))))
