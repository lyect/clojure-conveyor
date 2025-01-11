(ns blocks.node.definitions.node.def
  (:require [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.hierarchy               :as node-hierarchy]
            [blocks.node.properties              :as node-properties]
            [blocks.node.types                   :as node-types]))


(defn define []
  (dosync
    (when-not (node-types/defined? node-types/NodeT)
      (alter node-hierarchy/tree #(assoc % node-types/NodeT {node-properties/label           "Node"
                                                             node-properties/super-type-tag  nil
                                                             node-properties/inputs          nil
                                                             node-properties/outputs         nil
                                                             node-properties/links           nil
                                                             node-properties/inputs-map      nil
                                                             node-properties/outputs-map     nil
                                                             node-properties/links-map       nil
                                                             node-properties/fields-tags     base-node-fields/tags-list})))))
