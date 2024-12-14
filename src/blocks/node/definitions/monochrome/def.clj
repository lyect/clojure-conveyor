(ns blocks.node.definitions.monochrome.def
  (:require [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.definitions.monochrome.fields :as monochrome-node-fields]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))


(defn- monochrome-node-function
  "Apply monochrome filter to the _bitmap_"
  [node bitmap]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was monochromed"))
  bitmap)

(defn define-monochrome-node []
  (when-not (node-types/defined? node-types/MonochromeT)
    (node-base/define-node-type node-types/MonochromeT
      node-properties/inputs   [channel-types/BitmapT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function monochrome-node-function
      node-properties/fields   monochrome-node-fields/fields-list)))
