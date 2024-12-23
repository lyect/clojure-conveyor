(ns blocks.node.definitions.selector.def
  (:require [blocks.node.base :as node-base]
            [blocks.node.definitions.node.def :as base-node-def]
            [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.properties :as node-properties]
            [blocks.node.types :as node-types]))

(defn- selector-ready-validator [node-ref]
  (reduce
    (fn [res [input-buffer-amount input-buffer-ref]]
      (or res (<= input-buffer-amount (count @input-buffer-ref))))
    false
    (utils/zip (node-ref base-node-fields/input-buffers-amounts) (node-ref base-node-fields/input-buffers))))

(defn define-selector-node []
  (when-not (node-types/defined? node-types/SelectorT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/SelectorT
                                node-properties/ready-validator selector-ready-validator)))
