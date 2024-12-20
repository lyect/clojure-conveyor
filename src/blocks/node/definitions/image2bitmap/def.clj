(ns blocks.node.definitions.image2bitmap.def
  (:require [blocks.channel.definitions.image.def :as image-channel-def]
            [blocks.channel.definitions.bitmap.def :as bitmap-channel-def]
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

(defn- image2bitmap-ready-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (seq? @input-buffer-ref)))

(defn- image2bitmap-inputs-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (channel-types/subtype? (channel-methods/get-channel-type-name (first @input-buffer-ref)) channel-types/ImageT)))

(defn- image2bitmap-node-function
  [node-ref]
  (let [node-fields       (node-ref node-properties/fields)
        input-buffer-ref  (get (node-fields base-node-fields/input-buffers)  0)
        output-buffer-ref (get (node-fields base-node-fields/output-buffers) 0)
        image             (first @input-buffer-ref)
        width             (channel-methods/get-channel-field image image-channel-fields/width)
        height            (channel-methods/get-channel-field image image-channel-fields/height)]
    (alter input-buffer-ref #(rest @input-buffer-ref))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Image2Bitmap done: Bitmap(width: " width ", height: " height ")"))
    (alter output-buffer-ref #(conj % (channel-methods/create channel-types/BitmapT
                                                              image-channel-fields/width  width
                                                              image-channel-fields/height height)))))

(defn define-image2bitmap-node []
  (when-not (node-types/defined? node-types/Image2BitmapT)
    (base-node-def/define-base-node)
    (image-channel-def/define-image-channel)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/Image2BitmapT
      node-properties/inputs           [channel-types/ImageT]
      node-properties/outputs          [channel-types/BitmapT]
      node-properties/function         image2bitmap-node-function
      node-properties/ready-validator  image2bitmap-ready-validator
      node-properties/inputs-validator image2bitmap-inputs-validator
      node-properties/fields           image2bitmap-node-fields/fields-list)))
