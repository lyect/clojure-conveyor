(ns blocks.node.definitions.rgbsplit.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.definitions.rgbsplit.fields :as rgbsplit-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.input.methods               :as node-input-methods]
            [blocks.node.link.methods                :as node-link-methods]
            [blocks.node.output.methods              :as node-output-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))


(def image-input  ::image-input)
(def red-output   ::red-output)
(def green-output ::green-output)
(def blue-output  ::blue-output)

(defn- handler
  [_ [image]]
  (let [rw (channel-methods/get-field-value image image-channel-fields/width)
        rh (channel-methods/get-field-value image image-channel-fields/height)
        gw (channel-methods/get-field-value image image-channel-fields/width)
        gh (channel-methods/get-field-value image image-channel-fields/height)
        bw (channel-methods/get-field-value image image-channel-fields/width)
        bh (channel-methods/get-field-value image image-channel-fields/height)
        rr (channel-methods/create channel-types/BitmapT
                                   image-channel-fields/width  rw
                                   image-channel-fields/height rh)
        gr (channel-methods/create channel-types/BitmapT
                                   image-channel-fields/width  gw
                                   image-channel-fields/height gh)
        br (channel-methods/create channel-types/BitmapT
                                   image-channel-fields/width  bw
                                   image-channel-fields/height bh)]
    (println (str "RGBSplit " image))
    (println (str "RGBSplit result: " rr "(W=" rw ", H=" rh ") " gr "(W=" gw ", H=" gh ") " br "(W=" bw ", H=" bh ")"))
    {red-output   [rr]
     green-output [gr]
     blue-output  [br]}))

(defn define []
  (when-not (node-types/defined? node-types/RGBSplitT)
    (base-node-def/define)
    (bitmap-channel-def/define)
    (node-types/define "RGBSplit" node-types/RGBSplitT
                       node-properties/inputs      [(node-input-methods/create image-input
                                                                               channel-types/ImageT)]
                       node-properties/outputs     [(node-output-methods/create red-output
                                                                                channel-types/ImageT)
                                                    (node-output-methods/create green-output
                                                                                channel-types/ImageT)
                                                    (node-output-methods/create blue-output
                                                                                channel-types/ImageT)]
                       node-properties/links       [(node-link-methods/create [image-input]
                                                                              [red-output
                                                                               green-output
                                                                               blue-output]
                                                                              handler)]
                       node-properties/fields-tags rgbsplit-node-fields/tags-list)))
