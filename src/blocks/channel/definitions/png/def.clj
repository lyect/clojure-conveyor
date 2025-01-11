(ns blocks.channel.definitions.png.def
  (:require [blocks.channel.definitions.image.def  :as image-channel-def]
            [blocks.channel.definitions.png.fields :as png-channel-fields]
            [blocks.channel.properties             :as channel-properties]
            [blocks.channel.types                  :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/PngT)
    (image-channel-def/define)
    (channel-types/define "Png" channel-types/PngT
                          channel-properties/super-type-tag channel-types/ImageT
                          channel-properties/fields-tags    png-channel-fields/tags-list)))
