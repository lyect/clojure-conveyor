(ns blocks.channel.definitions.jpeg.def
  (:require [blocks.channel.definitions.image.def   :as image-channel-def]
            [blocks.channel.definitions.jpeg.fields :as jpeg-channel-fields]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]))
  
  
(defn define []
  (when-not (channel-types/defined? channel-types/JpegT)
    (image-channel-def/define)
    (channel-types/define "Jpeg" channel-types/JpegT
                          channel-properties/super-type-tag channel-types/ImageT
                          channel-properties/fields-tags    jpeg-channel-fields/tags-list)))
