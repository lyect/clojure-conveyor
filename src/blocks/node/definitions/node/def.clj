(ns blocks.node.definitions.node.def
  (:require [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.hierarchy               :as node-hierarchy]
            [blocks.node.properties              :as node-properties]
            [blocks.node.types                   :as node-types]))

(defn define-base-node []
  (when-not (node-hierarchy/tree node-types/NodeT)
    (alter node-hierarchy/tree #(assoc % node-types/NodeT {node-properties/type-name        node-types/NodeT
                                                           node-properties/super-name       nil
                                                           node-properties/inputs           nil
                                                           node-properties/outputs          nil
                                                           node-properties/function         nil
                                                           node-properties/ready-validator  nil
                                                           node-properties/inputs-validator nil
                                                           node-properties/fields           base-node-fields/fields-list}))))