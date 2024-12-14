(ns blocks.node.definitions.jpeg2bitmap.def
  (:require [blocks.channel.definitions.bitmap.fields   :as bitmap-channel-fields]
            [blocks.channel.definitions.jpeg.fields     :as jpeg-channel-fields]
            [blocks.channel.methods                     :as channel-methods]
            [blocks.channel.types                       :as channel-types]
            [blocks.node.base                           :as node-base]
            [blocks.node.definitions.jpeg2bitmap.fields :as jpeg2bitmap-node-fields]
            [blocks.node.methods                        :as node-methods]
            [blocks.node.properties                     :as node-properties]
            [blocks.node.types                          :as node-types]))


(defn- jpeg2bitmap-node-function
  "Transform JPEG to Bitmap"
  [node jpeg]
  (let [jpeg-width  (channel-methods/get-channel-field jpeg jpeg-channel-fields/width)
        jpeg-height (channel-methods/get-channel-field jpeg jpeg-channel-fields/height)
        bitmap (channel-methods/create channel-types/BitmapT
                                       bitmap-channel-fields/width  jpeg-width
                                       bitmap-channel-fields/height jpeg-height)]
    (println (str "[" (node-methods/get-node-name node) "]: " jpeg " was transformed to " bitmap))
    bitmap))

(defn define-jpeg2bitmap-node []
  (when-not (node-types/defined? node-types/Jpeg2BitmapT)
    (node-base/define-node-type node-types/Jpeg2BitmapT
      node-properties/inputs   [channel-types/JpegT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function jpeg2bitmap-node-function
      node-properties/fields   jpeg2bitmap-node-fields/fields-list)))
