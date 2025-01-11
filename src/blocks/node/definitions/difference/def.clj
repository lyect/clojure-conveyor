(ns blocks.node.definitions.difference.def
  (:require [blocks.channel.definitions.bitmap.def     :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields   :as image-channel-fields]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.definitions.difference.fields :as difference-node-fields]
            [blocks.node.definitions.node.def          :as base-node-def]
            [blocks.node.input.methods                 :as node-input-methods]
            [blocks.node.link.methods                  :as node-link-methods]
            [blocks.node.output.methods                :as node-output-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))


(def l-image-input ::l-image-input)
(def r-image-input ::r-image-input)
(def result-output ::result-output)

(defn- handler
  [_ [l-image] [r-image]]
  (let [l-image-width      (channel-methods/get-field-value l-image image-channel-fields/width)
        r-image-width      (channel-methods/get-field-value r-image image-channel-fields/width)
        l-image-height     (channel-methods/get-field-value l-image image-channel-fields/height)
        r-image-height     (channel-methods/get-field-value r-image image-channel-fields/height)
        max-width          (max l-image-width r-image-width)
        max-height         (max l-image-height r-image-height)
        result             (channel-methods/create channel-types/BitmapT
                                                   image-channel-fields/width  max-width
                                                   image-channel-fields/height max-height)]
    (println (str "Difference " l-image ", " r-image))
    (println (str "Difference result: " result "(W=" max-width ", H=" max-height ")"))
    {result-output [result]}))

(defn define []
  (when-not (node-types/defined? node-types/DifferenceT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "Difference" node-types/DifferenceT
                       node-properties/inputs      [(node-input-methods/create l-image-input
                                                                               channel-types/ImageT)
                                                    (node-input-methods/create r-image-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create result-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [l-image-input
                                                                               r-image-input]
                                                                              [result-output]
                                                                              handler)]
                       node-properties/fields-tags difference-node-fields/tags-list)))
