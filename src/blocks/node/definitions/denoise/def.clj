(ns blocks.node.definitions.denoise.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.definitions.denoise.fields  :as denoise-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.input.methods               :as node-input-methods]
            [blocks.node.link.methods                :as node-link-methods]
            [blocks.node.output.methods              :as node-output-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))


(def image-input   ::image-input)
(def result-output ::result-output)

(defn- handler
  [_ [image]]
  (let [width  (channel-methods/get-field-value image image-channel-fields/width)
        height (channel-methods/get-field-value image image-channel-fields/height)
        result (channel-methods/create channel-types/BitmapT
                                       image-channel-fields/width  width
                                       image-channel-fields/height height)]
    (println (str "Denoise " image))
    (println (str "Denoise result: " result "(W=" width ", H=" height ")"))
    {result-output [result]}))

(defn define []
  (when-not (node-types/defined? node-types/DenoiseT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "Denoise" node-types/DenoiseT
                       node-properties/inputs      [(node-input-methods/create image-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create result-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [image-input]
                                                                              [result-output]
                                                                              handler)]
                       node-properties/fields-tags denoise-node-fields/tags-list)))
