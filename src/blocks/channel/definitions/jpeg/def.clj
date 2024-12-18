(ns blocks.channel.definitions.jpeg.def
  (:require [blocks.channel.base                    :as channel-base]
            [blocks.channel.definitions.image.def   :as image-channel-def]
            [blocks.channel.definitions.jpeg.fields :as jpeg-channel-fields]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]))
  
  
(defn define-jpeg-channel []
  (when-not (channel-types/defined? channel-types/JpegT)
    (image-channel-def/define-image-channel)
    (channel-base/define-channel-type channel-types/JpegT
                                      channel-properties/super-name channel-types/ImageT
                                      channel-properties/fields jpeg-channel-fields/fields-list)))
