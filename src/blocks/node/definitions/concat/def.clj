(ns blocks.node.definitions.concat.def
  (:require [blocks.channel.definitions.image.fields :as image-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.base                        :as node-base]
            [blocks.node.definitions.concat.fields   :as concat-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.methods                     :as node-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))


(defn- concat-node-function
  "Apply concat to the _image_"
  [node]
  (alter buffer #(conj % image))
  (println (str "[" (node-methods/get-node-name node) "]: Added " image " to buffer"))
  (when (= (count @buffer) (node-methods/get-node-field node concat-node-fields/n))
    (let [sum-width  (apply +   (map #(channel-methods/get-channel-field % image-fields/width)  @buffer))
          max-height (apply max (map #(channel-methods/get-channel-field % image-fields/height) @buffer))
          bitmap     (channel-methods/create channel-types/BitmapT
                                             image-fields/width  sum-width
                                             image-fields/height max-height)]
      (println (str "[" (node-methods/get-node-name node) "]: Concat result: " bitmap))
      (alter buffer #(vector))
      (println (str "[" (node-methods/get-node-name node) "]: Cleared buffer"))
      bitmap)))

(defn define-concat-node []
  (when-not (node-types/defined? node-types/ConcatT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/ConcatT
      node-properties/inputs   [channel-types/ImageT]
      node-properties/outputs  [channel-types/ImageT]
      node-properties/function concat-node-function
      node-properties/fields   concat-node-fields/fields-list)))
