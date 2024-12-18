(ns blocks.channel.definitions.png.def
  (:require [blocks.channel.base                   :as channel-base]
            [blocks.channel.definitions.image.def  :as image-channel-def]
            [blocks.channel.definitions.png.fields :as png-channel-fields]
            [blocks.channel.properties             :as channel-properties]
            [blocks.channel.types                  :as channel-types]))


(defn define-png-channel []
  (when-not (channel-types/defined? channel-types/PngT)
    (image-channel-def/define-image-channel)
    (channel-base/define-channel-type channel-types/PngT
                                      channel-properties/super-name channel-types/ImageT
                                      channel-properties/fields png-channel-fields/fields-list)))
