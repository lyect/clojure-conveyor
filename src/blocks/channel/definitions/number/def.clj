(ns blocks.channel.definitions.number.def
  (:require [blocks.channel.definitions.channel.def   :as base-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.base                      :as channel-base]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-number-channel []
  (when-not (channel-types/defined? channel-types/NumberT)
    (base-channel-def/define-base-channel)
    (channel-base/define-channel-type channel-types/NumberT
                                      channel-properties/super-name channel-types/ChannelT
                                      channel-properties/fields     number-channel-fields/fields-list)))
