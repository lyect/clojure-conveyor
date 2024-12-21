(ns blocks.node.definitions.image2bitmap.def
  (:require [blocks.channel.definitions.bitmap.def       :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields     :as image-channel-fields]
            [blocks.channel.methods                      :as channel-methods]
            [blocks.channel.types                        :as channel-types]
            [blocks.node.base                            :as node-base]
            [blocks.node.definitions.node.fields         :as base-node-fields]
            [blocks.node.definitions.node.def            :as base-node-def]
            [blocks.node.definitions.image2bitmap.fields :as image2bitmap-node-fields]
            [blocks.node.methods                         :as node-methods]
            [blocks.node.properties                      :as node-properties]
            [blocks.node.types                           :as node-types]))


(defn- image2bitmap-node-function
  [node-ref]
  (let [input-buffer-ref  (nth (node-ref base-node-fields/input-buffers)  0)
        output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)
        image             (first @input-buffer-ref)
        width             (channel-methods/get-channel-field image image-channel-fields/width)
        height            (channel-methods/get-channel-field image image-channel-fields/height)]
    (alter input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Image2Bitmap done: Bitmap(width: " width ", height: " height ")"))
    (alter output-buffer-ref #(conj % (channel-methods/create channel-types/BitmapT
                                                              image-channel-fields/width  width
                                                              image-channel-fields/height height)))))

(defn define-image2bitmap-node []
  (when-not (node-types/defined? node-types/Image2BitmapT)
    (base-node-def/define-base-node)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/Image2BitmapT
      node-properties/inputs   [channel-types/ImageT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function image2bitmap-node-function
      node-properties/fields   image2bitmap-node-fields/fields-list)))
