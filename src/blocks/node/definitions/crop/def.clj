(ns blocks.node.definitions.crop.def
  (:require [blocks.channel.definitions.bitmap.fields :as bitmap-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.node.base                         :as node-base]
            [blocks.node.definitions.crop.fields      :as crop-node-fields]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.properties                   :as node-properties]
            [blocks.node.types                        :as node-types]))
  

(defn- crop-node-function
  "The returned bitmap is copied from the position (_x_, _y_) in _bitmap_, and will always have the given width=_w_ and height=_h_."
  [node bitmap x y w h]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was cropped: offset=(" x ", " y "), rect=(" w ", " h ")"))
  (channel-methods/create channel-types/BitmapT
                          bitmap-channel-fields/width  w
                          bitmap-channel-fields/height h))

(defn define-crop-node []
  (when-not (node-types/defined? node-types/CropT)
    (node-base/define-node-type node-types/CropT
                                node-properties/inputs   [channel-types/BitmapT
                                                          channel-types/IntegerT
                                                          channel-types/IntegerT
                                                          channel-types/IntegerT
                                                          channel-types/IntegerT]
                                node-properties/outputs  [channel-types/BitmapT]
                                node-properties/function crop-node-function
                                node-properties/fields   crop-node-fields/fields-list)))
