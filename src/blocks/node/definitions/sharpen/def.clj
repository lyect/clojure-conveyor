(ns blocks.node.definitions.sharpen.def
  (:require [blocks.channel.types                   :as channel-types]
            [blocks.node.base                       :as node-base]
            [blocks.node.definitions.sharpen.fields :as sharpen-node-fields]
            [blocks.node.methods                    :as node-methods]
            [blocks.node.properties                 :as node-properties]
            [blocks.node.types                      :as node-types]))


(defn- sharpen-node-function
  "Apply sharpen kernel filter to the _bitmap_"
  [node bitmap kernel]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was sharpened with kernel=" kernel))
  bitmap)

(defn define-sharpen-node []
  (when-not (node-types/defined? node-types/SharpenT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/SharpenT
      node-properties/inputs   [channel-types/BitmapT
                                channel-types/MatrixT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function sharpen-node-function
      node-properties/fields   sharpen-node-fields/fields-list)))
