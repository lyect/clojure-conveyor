(ns blocks.channel.definitions.number.def
  (:require [blocks.channel.definitions.channel.def   :as base-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/NumberT)
    (base-channel-def/define)
    (channel-types/define "Number" channel-types/NumberT
                          channel-properties/super-type-tag channel-types/ChannelT
                          channel-properties/fields-tags    number-channel-fields/tags-list)))
