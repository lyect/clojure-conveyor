(ns blocks.node.definitions.concat.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.types                       :as node-types]
            [blocks.node.definitions.concat.fields   :as concat-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.input.methods :as node-input-methods]
            [blocks.node.output.methods :as node-output-methods]
            [blocks.node.link.methods :as node-link-methods]))


(def images-input  ::images-input)
(def result-output ::result-output)

(defn- handler
  [_ images]
  (let [[sum-width max-height] (reduce
                                (fn [[sum-width max-height] image]
                                  [(+   sum-width  (channel-methods/get-field-value image image-channel-fields/width))
                                   (max max-height (channel-methods/get-field-value image image-channel-fields/height))])
                                [0 0]
                                images)
        result                 (channel-methods/create channel-types/BitmapT
                                                       image-channel-fields/width  sum-width
                                                       image-channel-fields/height max-height)]
    (println (reduce #(str %1 " " %2) "Concat " images))
    (println (str "Concat result: " result "(W=" sum-width ", H=" max-height ")"))
    {result-output [result]}))

(defn define []
  (when-not (node-types/defined? node-types/ConcatT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "Concat" node-types/ConcatT
                       node-properties/inputs      [(node-input-methods/create images-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create result-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [images-input]
                                                                              [result-output]
                                                                              handler)]
                       node-properties/fields-tags concat-node-fields/tags-list)))
