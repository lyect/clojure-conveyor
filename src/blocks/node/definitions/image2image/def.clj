(ns blocks.node.definitions.image2image.def
  (:require [blocks.channel.definitions.bitmap.def :as bitmap-channel-def]
            [blocks.channel.definitions.jpeg.def :as jpeg-channel-def]
            [blocks.channel.definitions.png.def :as png-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.definitions.jpeg.fields :as jpeg-channel-fields]
            [blocks.channel.definitions.png.fields :as png-channel-fields]
            [blocks.channel.methods :as channel-methods]
            [blocks.channel.types :as channel-types]
            [blocks.node.base :as node-base]
            [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.methods :as node-methods]
            [blocks.node.properties :as node-properties]
            [blocks.node.types :as node-types]
            [blocks.node.definitions.selector.def :as selector-node-def]
            [blocks.node.definitions.image2image.fields :as image2image-node-fields]))

(defn- image2jpeg [buffer-ref]
  (jpeg-channel-def/define-jpeg-channel)
  (let [image (first @buffer-ref)]
    (when-not (nil? image)
      (alter buffer-ref #(rest %))
      (let [width (channel-methods/get-channel-field image image-channel-fields/width)
            height (channel-methods/get-channel-field image image-channel-fields/height)
            jpeg-image  (channel-methods/create channel-types/JpegT
                                                image-channel-fields/width width
                                                image-channel-fields/height height
                                                jpeg-channel-fields/color-space "RGB"
                                                jpeg-channel-fields/chroma-subsampling-scheme :std)]
        (println (str "Image2Jpeg done: Jpeg (width: " width ", height: " height ")"))
        jpeg-image))))

(defn- image2png [buffer-ref]
  (png-channel-def/define-png-channel)
  (let [image (first @buffer-ref)]
    (when-not (nil? image)
      (alter buffer-ref #(rest %))
      (let [width (channel-methods/get-channel-field image image-channel-fields/width)
            height (channel-methods/get-channel-field image image-channel-fields/height)
            png-image (channel-methods/create channel-types/PngT
                                              image-channel-fields/width width
                                              image-channel-fields/height height
                                              png-channel-fields/alpha-used true)]
        (println (str "Image2Png done: Png (width: " width ", height: " height ")"))
        png-image))))

(defn- image2bitmap [buffer-ref]
  (bitmap-channel-def/define-bitmap-channel)
  (let [image (first @buffer-ref)]
    (when-not (nil? image)
      (alter buffer-ref #(rest %))
      (let [width (channel-methods/get-channel-field image image-channel-fields/width)
            height (channel-methods/get-channel-field image image-channel-fields/height)
            bitmap (channel-methods/create channel-types/BitmapT
                                           image-channel-fields/width  width
                                           image-channel-fields/height height)]
        (println (str "Image2Bitmap done: Bitmap (width: " width ", height: " height ")"))
        bitmap))))

(defn- image2image-node-function [node-ref]
  (println "Image2Image started!")
  (let [input-buffer1-ref (nth (node-ref base-node-fields/input-buffers) 0)
        input-buffer2-ref (nth (node-ref base-node-fields/input-buffers) 1)
        input-buffer3-ref (nth (node-ref base-node-fields/input-buffers) 2)
        output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)]
    (print (str "[" (node-methods/get-node-name node-ref) "]: "))
    (let [image (or (image2jpeg   input-buffer1-ref)
                    (image2png    input-buffer2-ref)
                    (image2bitmap input-buffer3-ref))]
      (alter output-buffer-ref #(conj % image)))))

(defn define-image2image-node []
  (when-not (node-types/defined? node-types/Image2ImageT)
    (selector-node-def/define-selector-node)
    (node-base/define-node-type node-types/Image2ImageT
                                node-properties/super-name node-types/SelectorT
                                node-properties/inputs     [channel-types/ImageT
                                                            channel-types/ImageT
                                                            channel-types/ImageT]
                                node-properties/outputs    [channel-types/ImageT]
                                node-properties/function   image2image-node-function
                                node-properties/fields     image2image-node-fields/fields-list)))
