(ns blocks.node.definitions.rgbsplit.def
  (:require [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.base                        :as node-base]
            [blocks.node.definitions.base.fields     :as base-node-fields]
            [blocks.node.definitions.rgbsplit.fields :as rgbsplit-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.methods                     :as node-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))

(defn- rgbsplit-ready-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (seq? @input-buffer-ref)))

(defn- rgbsplit-inputs-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (channel-types/subtype? (channel-methods/get-channel-type-name (first @input-buffer-ref)) channel-types/ImageT)))

(defn- rgbsplit-node-function
  [node-ref]
  (let [node-fields         (node-ref node-properties/fields)
        input-buffer-ref    (get (node-fields base-node-fields/input-buffers)  0)
        r-output-buffer-ref (get (node-fields base-node-fields/output-buffers) 0)
        g-output-buffer-ref (get (node-fields base-node-fields/output-buffers) 1)
        b-output-buffer-ref (get (node-fields base-node-fields/output-buffers) 2)
        image               (first @input-buffer-ref)
        width               (channel-methods/get-channel-field image image-channel-fields/width)
        height              (channel-methods/get-channel-field image image-channel-fields/height)]
    (alter input-buffer-ref #(rest @input-buffer-ref))
    (println (str "[" (node-methods/get-node-name node-ref) "]: RGBSplit done for Image()"))
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
    (node-base/define-node-type node-types/RGBSplitT
      node-properties/inputs           [channel-types/ImageT]
      node-properties/outputs          [channel-types/BitmapT
                                        channel-types/BitmapT
                                        channel-types/BitmapT]
      node-properties/function         rgbsplit-node-function
      node-properties/ready-validator  rgbsplit-ready-validator
      node-properties/inputs-validator rgbsplit-inputs-validator
      node-properties/fields           rgbsplit-node-fields/fields-list)))
