(ns blocks.node.definitions.node.def
  (:require [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.hierarchy               :as node-hierarchy]
            [blocks.node.properties              :as node-properties]
            [blocks.node.types                   :as node-types]
            [utils]))

(defn- base-node-ready-validator [node-ref]
  (reduce
    (fn [res [input-buffer-amount input-buffer-ref]]
      (and res (<= input-buffer-amount (count @input-buffer-ref))))
    true
    (utils/zip (node-ref base-node-fields/input-buffers-amounts) (node-ref base-node-fields/input-buffers))))

(defn define-base-node []
  (dosync
    (when-not (node-hierarchy/tree node-types/NodeT)
      (alter node-hierarchy/tree #(assoc % node-types/NodeT {node-properties/type-name       node-types/NodeT
                                                             node-properties/super-name      nil
                                                             node-properties/inputs          nil
                                                             node-properties/outputs         nil
                                                             node-properties/ready-validator base-node-ready-validator
                                                             node-properties/function        nil
                                                             node-properties/fields          base-node-fields/fields-list})))))
