(ns blocks.node.base
  (:require [clojure.set            :as cljset]
            [blocks.channel.types   :as channel-types]
            [blocks.node.exceptions :as node-exceptions]
            [blocks.node.hierarchy  :as node-hierarchy]
            [blocks.node.properties :as node-properties]
            [blocks.node.types      :as node-types]
            [utils]
            [blocks.node.base :as node-base]))


;; +-------------------------+
;; |                         |
;; |   CHANNEL BASE FIELDS   |
;; |                         |
;; +-------------------------+

(def type-name ::nodefield-type-name)
(def node-name ::nodefield-name)

;; +---------------------+
;; |                     |
;; |   NODE VALIDATORS   |
;; |                     |
;; +---------------------+

(defn- validate-function [function]
  (fn? function))

(defn- validate-inputs [inputs]
  (reduce
   #(and %1 (utils/in-list? channel-types/types-list %2))
   true
   inputs))

(defn- validate-outputs [outputs]
  (reduce
   #(and %1 (utils/in-list? channel-types/types-list %2))
   true
   outputs))

;; +------------------------------------------+
;; |                                          |
;; |   NODE TYPE DEFINING RELATED FUNCTIONS   |
;; |                                          |
;; +------------------------------------------+

; Alters hierarchy tree with Node (base type for all the node types) only once
(dosync (when-not (node-hierarchy/tree node-types/Node)
          (alter node-hierarchy/tree #(assoc % node-types/Node {node-properties/type-name  node-types/Node
                                                                node-properties/super-name nil
                                                                node-properties/inputs     nil
                                                                node-properties/outputs    nil
                                                                node-properties/function   nil
                                                                node-properties/fields     (list node-name)}))))

(defn append-node-hierarchy
  "Alter tree hierarchy with _new-node-type_ if not defined and super is defined"
  [new-node-type]
  (let [new-node-type-name (new-node-type node-properties/type-name)]
    (when (node-hierarchy/tree new-node-type-name)
      (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/type-defined
                                        (str "Type named \"" new-node-type-name "\" is already defined"))))
    (when-not (validate-inputs (new-node-type node-properties/inputs))
      (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/inputs-unvalidated
                                        (str "Inputs of type named \"" new-node-type-name "\" are unvalidated"))))
    (when-not (validate-outputs (new-node-type node-properties/outputs))
      (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/outputs-unvalidated
                                        (str "Outputs of type named \"" new-node-type-name "\" are unvalidated"))))
    (when-not (validate-function (new-node-type node-properties/function))
      (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/function-unvalidated
                                        (str "Function of type named \"" new-node-type-name " is unvalidated"))))
    (dosync (alter node-hierarchy/tree #(assoc % new-node-type-name new-node-type)))))

(defmacro define-node-type
  "Define a node with _type-name_ and _properties_"
  [new-node-type-name & properties]
  `(let [properties-map# (hash-map ~@properties)
         super-name#     (or (properties-map# node-properties/super-name) node-types/Node)]
     (when-not (utils/in-list? node-types/types-list ~new-node-type-name)
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/type-undeclared
                                         (str "Type with name \"" ~new-node-type-name "\" is not declared"))))
     (when-not (utils/in-list? node-types/types-list super-name#)
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/super-undeclared
                                         (str "Super \"" super-name# "\" of \"" ~new-node-type-name "\" is undeclared"))))
     (when-not (node-hierarchy/tree super-name#)
       (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/super-undefined
                                         (str "Super \"" super-name# "\" of \"" ~new-node-type-name "\" is undefined"))))
     (let [super#               (node-hierarchy/tree super-name#)
           new-type-fields#     (properties-map# node-properties/fields)
           super-fields#        (super# node-properties/fields)
           new-type-fields-set# (set new-type-fields#)
           super-fields-set#    (set super-fields#)]
       (when-not (<= (count new-type-fields#) (count new-type-fields-set#))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/duplicating-fields
                                           (str "Fields of type named \"" ~new-node-type-name "\" are duplicated"))))
       (when-not (empty? (cljset/intersection new-type-fields-set# super-fields-set#))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/super-fields-intersection
                                           (str "Fields of type named \"" ~new-node-type-name "\" intersect with fields of super type named \"" super-name# "\""))))
       (when-not (or (properties-map# node-properties/inputs) (super# node-properties/inputs))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/inputs-undefined
                                           (str "Inputs of type named \"" ~new-node-type-name "\" is undefined (neither type nor super have defined inputs)"))))
       (when-not (or (properties-map# node-properties/outputs) (super# node-properties/outputs))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/outputs-undefined
                                           (str "Outputs of type named \"" ~new-node-type-name "\" is undefined (neither type nor super have defined outputs)"))))
       (when-not (or (properties-map# node-properties/function) (super# node-properties/function))
         (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/function-undefined
                                           (str "Function of type named \"" ~new-node-type-name "\" is undefined (neither type nor super have defined function)"))))
       (append-node-hierarchy {node-properties/type-name  ~new-node-type-name
                               node-properties/super-name super-name#
                               node-properties/inputs     (or     (properties-map# node-properties/inputs)   (super# node-properties/inputs))
                               node-properties/outputs    (or     (properties-map# node-properties/outputs)  (super# node-properties/outputs))
                               node-properties/function   (or     (properties-map# node-properties/function) (super# node-properties/function))
                               node-properties/fields     (concat new-type-fields# super-fields#)})
       (when-not (utils/lists-equal? node-properties/properties-list (keys (node-hierarchy/tree ~new-node-type-name)))
         (do
           (dosync (alter node-hierarchy/tree #(dissoc % ~new-node-type-name)))
           (throw (node-exceptions/construct node-exceptions/define-node-type node-exceptions/node-properties-missing
                                             "Not all node-properties are added to define-node-type macro")))))))
