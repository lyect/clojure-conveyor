(ns blocks.node.definitions.rgbsplit.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.base                        :as node-base]
            [blocks.node.definitions.node.fields     :as base-node-fields]
            [blocks.node.definitions.rgbsplit.fields :as rgbsplit-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.methods                     :as node-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))

(defn- rgbsplit-node-function
  [node-ref]
  (let [input-buffer-ref    (nth (node-ref base-node-fields/input-buffers)  0)
        r-output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)
        g-output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 1)
        b-output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 2)
        image               (first @input-buffer-ref)
        width               (channel-methods/get-channel-field image image-channel-fields/width)
        height              (channel-methods/get-channel-field image image-channel-fields/height)]
    (alter input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: RGBSplit done for Image: " @image))
    (alter r-output-buffer-ref #(conj % (channel-methods/create channel-types/BitmapT
                                                                image-channel-fields/width  width
                                                                image-channel-fields/height height)))
    (alter g-output-buffer-ref #(conj % (channel-methods/create channel-types/BitmapT
                                                                image-channel-fields/width  width
                                                                image-channel-fields/height height)))
    (alter b-output-buffer-ref #(conj % (channel-methods/create channel-types/BitmapT
                                                                image-channel-fields/width  width
                                                                image-channel-fields/height height)))))

(defn define-rgbsplit-node []
  (when-not (node-types/defined? node-types/RGBSplitT)
    (base-node-def/define-base-node)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/RGBSplitT
      node-properties/inputs   [channel-types/ImageT]
      node-properties/outputs  [channel-types/ImageT
                                channel-types/ImageT
                                channel-types/ImageT]
      node-properties/function rgbsplit-node-function
      node-properties/fields   rgbsplit-node-fields/fields-list)))
