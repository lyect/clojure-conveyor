(ns blocks.node.definitions.image2image.def
  (:require [blocks.channel.definitions.bitmap.def      :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields    :as image-channel-fields]
            [blocks.channel.definitions.jpeg.fields     :as jpeg-channel-fields]
            [blocks.channel.definitions.jpeg.def        :as jpeg-channel-def]
            [blocks.channel.definitions.png.fields      :as png-channel-fields]
            [blocks.channel.definitions.png.def         :as png-channel-def]
            [blocks.channel.methods                     :as channel-methods]
            [blocks.channel.types                       :as channel-types]
            [blocks.node.definitions.image2image.fields :as image2image-node-fields]
            [blocks.node.definitions.node.def           :as base-node-def]
            [blocks.node.input.methods                  :as node-input-methods]
            [blocks.node.link.methods                   :as node-link-methods]
            [blocks.node.output.methods                 :as node-output-methods]
            [blocks.node.properties                     :as node-properties]
            [blocks.node.types                          :as node-types]))


(def to-bitmap-input ::to-bitmap-input)
(def to-jpeg-input   ::to-jpeg-input)
(def to-png-input    ::to-png-input)
(def result-output   ::result-output)

(defn- to-bitmap-handler
  [_ [image]]
  (let [width  (channel-methods/get-field-value image image-channel-fields/width)
        height (channel-methods/get-field-value image image-channel-fields/height)
        result (channel-methods/create channel-types/BitmapT
                                       image-channel-fields/width  width
                                       image-channel-fields/height height)]
    (println (str "Image2Image (Bitmap) " image))
    (println (str "Image2Image (Bitmap) result: " result "(W=" width ", H=" height ")"))
    {result-output [result]}))

(defn- to-jpeg-handler
  [_ [image]]
  (let [width                     (channel-methods/get-field-value image image-channel-fields/width)
        height                    (channel-methods/get-field-value image image-channel-fields/height)
        color-space               "RGB"
        chroma-subsampling-scheme "4:1:1"
        result                    (channel-methods/create channel-types/JpegT
                                                          image-channel-fields/width                    width
                                                          image-channel-fields/height                   height
                                                          jpeg-channel-fields/color-space               color-space
                                                          jpeg-channel-fields/chroma-subsampling-scheme chroma-subsampling-scheme)]
    (println (str "Image2Image (JPEG) " image))
    (println (str "Image2Image (JPEG) result: " result "(W=" width ", H=" height ", CP=" color-space ", CSS=" chroma-subsampling-scheme ")"))
    {result-output [result]}))

(defn- to-png-handler
  [_ [image]]
  (let [width      (channel-methods/get-field-value image image-channel-fields/width)
        height     (channel-methods/get-field-value image image-channel-fields/height)
        alpha-used true
        result     (channel-methods/create channel-types/PngT
                                           image-channel-fields/width    width
                                           image-channel-fields/height   height
                                           png-channel-fields/alpha-used alpha-used)]
    (println (str "Image2Image (PNG) " image))
    (println (str "Image2Image (PNG) result: " result "(W=" width ", H=" height ", AU=" alpha-used ")"))
    {result-output [result]}))

(defn define []
  (when-not (node-types/defined? node-types/Image2ImageT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (jpeg-channel-def/define)
    (png-channel-def/define)
    (node-types/define "Image2Image" node-types/Image2ImageT
                       node-properties/inputs      [(node-input-methods/create to-bitmap-input
                                                                               channel-types/ImageT)
                                                    (node-input-methods/create to-jpeg-input
                                                                               channel-types/ImageT)
                                                    (node-input-methods/create to-png-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create result-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [to-bitmap-input]
                                                                              [result-output]
                                                                              to-bitmap-handler)
                                                    (node-link-methods/create [to-jpeg-input]
                                                                              [result-output]
                                                                              to-jpeg-handler)
                                                    (node-link-methods/create [to-png-input]
                                                                              [result-output]
                                                                              to-png-handler)]
                       node-properties/fields-tags image2image-node-fields/tags-list)))
