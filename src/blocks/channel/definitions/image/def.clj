(ns blocks.channel.definitions.image.def
  (:require [blocks.channel.definitions.channel.def  :as base-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.base                     :as channel-base]
            [blocks.channel.properties               :as channel-properties]
            [blocks.channel.types                    :as channel-types]))


(defn define-image-channel []
  (when-not (channel-types/defined? channel-types/ImageT)
    (base-channel-def/define-base-channel)
    (channel-base/define-channel-type channel-types/ImageT
                                      channel-properties/super-name channel-types/ChannelT
                                      channel-properties/fields     image-channel-fields/fields-list)))
