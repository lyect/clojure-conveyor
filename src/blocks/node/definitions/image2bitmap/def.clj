(ns blocks.node.definitions.png2bitmap.def
  (:require [blocks.channel.definitions.bitmap.fields  :as bitmap-channel-fields]
            [blocks.channel.definitions.png.fields     :as png-channel-fields]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.definitions.png2bitmap.fields :as png2bitmap-node-fields]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))


(defn- png2bitmap-node-function
  "Transform PNG to Bitmap"
  [node png]
  (let [png-width  (channel-methods/get-channel-field png png-channel-fields/width)
        png-height (channel-methods/get-channel-field png png-channel-fields/height)
        bitmap (channel-methods/create channel-types/BitmapT
                                       bitmap-channel-fields/width  png-width
                                       bitmap-channel-fields/height png-height)]
    (println (str "[" (node-methods/get-node-name node) "]: " png " was transformed to " bitmap))
    bitmap))

(defn define-png2bitmap-node []
  (when-not (node-types/defined? node-types/Png2BitmapT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/Png2BitmapT
      node-properties/inputs   [channel-types/PngT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function png2bitmap-node-function
      node-properties/fields   png2bitmap-node-fields/fields-list)))
