(ns blocks.node.definitions.inversion.def
  (:require [blocks.channel.types                     :as channel-types]
            [blocks.node.base                         :as node-base]
            [blocks.node.definitions.inversion.fields :as inversion-node-fields]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.properties                   :as node-properties]
            [blocks.node.types                        :as node-types]))


(defn- inversion-node-function
  "Apply inversion filter to the _bitmap_"
  [node bitmap]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was inverted"))
  bitmap)

(defn define-inversion-node []
  (when-not (node-types/defined? node-types/InversionT)
    (node-base/define-node-type node-types/InversionT
      node-properties/inputs   [channel-types/BitmapT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function inversion-node-function
      node-properties/fields   inversion-node-fields/fields-list)))
