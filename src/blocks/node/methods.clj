(ns blocks.node.methods
  (:require [clojure.set            :as cljset]
            [blocks.channel.methods :as channel-methods]
            [blocks.node.base       :as node-base]
            [blocks.node.exceptions :as node-exceptions]
            [blocks.node.hierarchy  :as node-hierarchy]
            [blocks.node.properties :as node-properties]
            [blocks.node.types      :as node-types]
            [utils]))


;; +-----------------------------+
;; |                             |
;; |   NODE RELATED PREDICATES   |
;; |                             |
;; +-----------------------------+

(defn node?
  "Predicate to check whether _obj_ is node or not"
  [obj]
  (and (some? obj)
       (map? obj)
       (contains? obj node-base/type-name)
       (node-types/defined? (obj node-base/type-name))
       (utils/lists-equal? (keys (dissoc obj node-base/type-name))
                           ((node-hierarchy/tree (obj node-base/type-name)) node-properties/fields))))

;; +----------------------------------------+
;; |                                        |
;; |   NODE TYPE RELATED PROPERTY GETTERS   |
;; |                                        |
;; +----------------------------------------+

;; No need to check whether node has the property or not. get-node-property is a private function,
;; hence it is used only for specific properties
(defn- get-node-property
  "Get _property_ of _node_"
  [node property]
  (when-not (node? node)
    (throw (node-exceptions/construct node-exceptions/get-node-property node-exceptions/not-node
                                      (str "\"" node "\" is not a node"))))
  ((node-hierarchy/tree (node node-base/type-name)) property))

;; No need to check whether "node" is a correct node or not
;; It will be done inside "get-node-property"
(defn get-node-type     [node] (get-node-property node node-properties/type-name))
(defn get-node-super    [node] (get-node-property node node-properties/super-name))
(defn get-node-inputs   [node] (get-node-property node node-properties/inputs))
(defn get-node-outputs  [node] (get-node-property node node-properties/outputs))
(defn get-node-function [node] (get-node-property node node-properties/function))
(defn get-node-fields   [node] (remove #{node-base/node-name} (get-node-property node node-properties/fields)))

;; +---------------------------------+
;; |                                 |
;; |   NODE INSTANCE FIELDS GETTER   |
;; |                                 |
;; +---------------------------------+

(defn get-node-name
  "Get name of _node_"
  [node]
  (when-not (node? node)
    (throw (node-exceptions/construct node-exceptions/get-node-name node-exceptions/not-node
                                      (str "\"" node "\" is not a node"))))
  (node node-base/node-name))

(defn get-node-field
  "Get field's value of _node_ by _field-name_"
  [node field-name]
  (when-not (node? node)
    (throw (node-exceptions/construct node-exceptions/get-node-field node-exceptions/not-node
                                      (str "\"" node "\" is not a node"))))
  (when-not (node field-name)
    (throw (node-exceptions/construct node-exceptions/get-node-field node-exceptions/unknown-field
                                      (str "\"" node "\" has no field named \"" field-name "\""))))
  (node field-name))

;; +----------------------+
;; |                      |
;; |   NODE CONSTRUCTOR   |
;; |                      |
;; +----------------------+

(defn create
  "Create node typed as _node-type-name_, fills fields with _fields_.
   _fields_ must be a collection consisting of pairs such as (f1 v1 f2 v2 ... fn vn)"
  [node-type-name node-name & fields]
  (when-not (utils/in-list? node-types/types-list node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undeclared
                                         (str "Type named \"" node-type-name "\" is undeclared"))))
  (when-not (node-types/defined? node-type-name)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undefined
                                         (str "Type named \"" node-type-name "\" is undefined"))))
  (when (= node-type-name node-types/Node)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/abstract-creation
                                         (str "Unable to instantiate abstract node"))))
  (let [fields-map      (apply hash-map fields)
        node-fields     (keys fields-map)
        node-fields-set (set node-fields)]
    (when (> (count (take-nth 2 fields)) (count node-fields-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/duplicating-fields
                                        (str "Tried to create " node-type-name " with duplicated fields"))))
    (let [all-fields-map       (assoc fields-map node-base/node-name node-name)
          node-all-fields      (keys all-fields-map)
          node-all-fields-set  (set node-all-fields)
          node-type-fields     ((node-hierarchy/tree node-type-name) node-properties/fields)
          node-type-fields-set (set node-type-fields)
          node                 (ref {})]

      (when-not (empty? (cljset/difference node-type-fields-set node-all-fields-set))
        (throw (node-exceptions/construct node-exceptions/create node-exceptions/missing-fields
                                          (str "Tried to create " node-type-name " with missing fields"))))
      (when-not (empty? (cljset/difference node-all-fields-set node-type-fields-set))
        (throw (node-exceptions/construct node-exceptions/create node-exceptions/excess-fields
                                          (str "Tried to create " node-type-name " with excess fields"))))
      (doseq [all-fields-map-entry all-fields-map]
        (alter node #(assoc % (first all-fields-map-entry) (second all-fields-map-entry))))
      (alter node #(assoc % node-base/type-name node-type-name)))))

;; +---------------------------+
;; |                           |
;; |   NODE ABSTRACT METHODS   |
;; |                           |
;; +---------------------------+

(defn- validate-input-parameter
  "Validate parameter _input-parameter_"
  [[input-parameter input-channel-type-name]]
  (when-not (channel-methods/channel? input-parameter)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/input-not-channel
                                      (str "\"" input-parameter "\" is not a channel"))))
  (when-not (= (channel-methods/get-channel-type-name input-parameter) input-channel-type-name)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/input-different-type
                                      (str "\"" input-parameter "\" has different from \"" input-channel-type-name "\" type name")))))

(defn- validate-output-parameter
  "Validate parameter _output-parameter_"
  [[output-parameter output-channel-type-name]]
  (when-not (channel-methods/channel? output-parameter)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/output-not-channel
                                      (str "\"" output-parameter "\" is not a channel"))))
  (when-not (= (channel-methods/get-channel-type-name output-parameter) output-channel-type-name)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/output-different-type
                                      (str "\"" output-parameter "\" has different from \"" output-channel-type-name "\" type name")))))

(defn execute
  [node & input-parameters]
  (when-not (node? node)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/not-node
                                      (str "\"" node "\" is not a node"))))
  (doall (map validate-input-parameter (utils/zip input-parameters (get-node-inputs node))))
  (let [output (apply (get-node-function node) node input-parameters)
        output-parameters (if (or (list? output) (vector? output)) output (vector output))]
    (doall (map validate-output-parameter (utils/zip output-parameters (get-node-outputs node))))
    output-parameters))
