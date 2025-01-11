(ns blocks.node.definitions.gamma.def
  (:require [blocks.channel.definitions.bitmap.def    :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields  :as image-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.node.definitions.gamma.fields     :as gamma-node-fields]
            [blocks.node.definitions.node.def         :as base-node-def]
            [blocks.node.input.methods                :as node-input-methods]
            [blocks.node.link.methods                 :as node-link-methods]
            [blocks.node.output.methods               :as node-output-methods]
            [blocks.node.properties                   :as node-properties]
            [blocks.node.types                        :as node-types]))


(def image-input   ::image-input)
(def gamma-input   ::gamma-input)
(def result-output ::result-output)

(defn- handler
  [_ [image] [gamma]]
  (let [width  (channel-methods/get-field-value image image-channel-fields/width)
        height (channel-methods/get-field-value image image-channel-fields/height)
        result (channel-methods/create channel-types/BitmapT
                                       image-channel-fields/width  width
                                       image-channel-fields/height height)]
    (println (str "Gamma " image " " gamma))
    (println (str "Gamma result: " result "(W=" width ", H=" height ")"))
    {result-output []}))

(defn define []
  (when-not (node-types/defined? node-types/GammaT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "Gamma" node-types/GammaT
                       node-properties/inputs      [(node-input-methods/create image-input
                                                                               channel-types/ImageT)
                                                    (node-input-methods/create gamma-input
                                                                               channel-types/NumberT)]
                       node-properties/outputs     [(node-output-methods/create result-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [image-input
                                                                               gamma-input]
                                                                              [result-output]
                                                                              handler)]
                       node-properties/fields-tags gamma-node-fields/tags-list)))
