(ns blocks.node.definitions.difference.def
  (:require [blocks.channel.definitions.bitmap.fields  :as bitmap-channel-fields]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.definitions.difference.fields :as difference-node-fields]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))


(defn- difference-node-function
  "The returned bitmap is difference between _bitmap1_ and _bitmap2_."
  [node bitmap1 bitmap2]
  (let [bitmap1-width  (channel-methods/get-channel-field bitmap1 bitmap-channel-fields/width)
        bitmap1-height (channel-methods/get-channel-field bitmap1 bitmap-channel-fields/height)
        bitmap2-width  (channel-methods/get-channel-field bitmap2 bitmap-channel-fields/width)
        bitmap2-height (channel-methods/get-channel-field bitmap2 bitmap-channel-fields/height)
        difference (channel-methods/create channel-types/BitmapT
                                           bitmap-channel-fields/width  (min bitmap1-width bitmap2-width)
                                           bitmap-channel-fields/height (min bitmap1-height bitmap2-height))]
    (println (str "[" (node-methods/get-node-name node) "]: Difference between " bitmap1 " and " bitmap2 " is " difference))
    difference))

(defn define-difference-node []
  (when-not (node-types/defined? node-types/DifferenceT)
    (node-base/define-node-type node-types/DifferenceT
      node-properties/inputs   [channel-types/BitmapT
                                channel-types/BitmapT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function difference-node-function
      node-properties/fields   difference-node-fields/fields-list)))
