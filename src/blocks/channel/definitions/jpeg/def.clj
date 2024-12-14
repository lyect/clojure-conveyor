(ns blocks.channel.definitions.jpeg.def
  (:require [blocks.channel.base                    :as channel-base]
            [blocks.channel.definitions.jpeg.fields :as jpeg-channel-fields]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]))
  
  
(defn define-jpeg-channel []
  (when-not (channel-types/defined? channel-types/JpegT)
    (channel-base/define-channel-type channel-types/JpegT
                                      channel-properties/fields jpeg-channel-fields/fields-list)))
