(ns blocks.node.definitions.cut.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.definitions.cut.fields      :as cut-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.input.methods               :as node-input-methods]
            [blocks.node.link.methods                :as node-link-methods]
            [blocks.node.output.methods              :as node-output-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))


(def image-input    ::image-input)
(def results-output ::results-output)

(defn- handler
  [node-fields [image]]
  (let [new-width  (quot (channel-methods/get-field-value image image-channel-fields/width) (node-fields cut-node-fields/n))
        new-height (channel-methods/get-field-value image image-channel-fields/height)
        results (into [] (repeatedly (node-fields cut-node-fields/n)
                                     (fn [] (channel-methods/create channel-types/BitmapT
                                                                    image-channel-fields/width  new-width
                                                                    image-channel-fields/height new-height))))]
    (println (str "Cut " image))
    (println (str "Cut result: " (reduce #(str %1 %2 "(W=" new-width ", H=" new-height ") ") "" results)))
    {results-output results}))

(defn define []
  (when-not (node-types/defined? node-types/CutT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "Cut" node-types/CutT
                       node-properties/inputs      [(node-input-methods/create image-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create results-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [image-input]
                                                                              [results-output]
                                                                              handler)]
                       node-properties/fields-tags cut-node-fields/tags-list)))
