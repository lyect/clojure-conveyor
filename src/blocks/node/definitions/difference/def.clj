(ns blocks.node.definitions.difference.def
  (:require [blocks.channel.definitions.bitmap.def     :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields   :as image-channel-fields]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.definitions.node.fields       :as base-node-fields]
            [blocks.node.definitions.difference.fields :as difference-node-fields]
            [blocks.node.definitions.node.def          :as base-node-def]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))

(defn- difference-node-function
  [node-ref]
  (let [l-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  0)
        r-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  1)
        output-buffer-ref  (nth (node-ref base-node-fields/output-buffers) 0)
        l-image            (first @l-input-buffer-ref)
        r-image            (first @r-input-buffer-ref)
        l-image-width      (channel-methods/get-channel-field @l-image image-channel-fields/width)
        r-image-width      (channel-methods/get-channel-field @r-image image-channel-fields/width)
        l-image-height     (channel-methods/get-channel-field @l-image image-channel-fields/height)
        r-image-height     (channel-methods/get-channel-field @r-image image-channel-fields/height)
        max-width          (max l-image-width r-image-width)
        max-height         (max l-image-height r-image-height)]
    (alter l-input-buffer-ref #(rest %))
    (alter r-input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Difference done: (width: " max-width ", height: " max-height ")"))
    (alter output-buffer-ref
           #(conj % (channel-methods/create channel-types/BitmapT
                                            image-channel-fields/width  max-width
                                            image-channel-fields/height max-height)))))

(defn define-difference-node []
  (when-not (node-types/defined? node-types/DifferenceT)
    (base-node-def/define-base-node)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/DifferenceT
      node-properties/inputs   [channel-types/ImageT
                                channel-types/ImageT]
      node-properties/outputs  [channel-types/ImageT]
      node-properties/function difference-node-function
      node-properties/fields   difference-node-fields/fields-list)))
