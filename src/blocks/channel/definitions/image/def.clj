(ns blocks.channel.definitions.image.def
  (:require [blocks.channel.definitions.channel.def  :as base-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.properties               :as channel-properties]
            [blocks.channel.types                    :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/ImageT)
    (base-channel-def/define)
    (channel-types/define "Image" channel-types/ImageT
                          channel-properties/super-type-tag channel-types/ChannelT
                          channel-properties/fields-tags    image-channel-fields/tags-list)))
