(ns blocks.channel.definitions.number.def
  (:require [blocks.channel.definitions.channel.def   :as base-channel-def]
            [blocks.channel.definitions.number.fields :as number-channel-fields]
            [blocks.channel.hierarchy                 :as channel-hierarchy]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-number-channel []
  (when-not (channel-types/defined? channel-types/NumberT)
    (base-channel-def/define-base-channel)
    (alter channel-hierarchy/tree #(assoc % channel-types/NumberT {channel-properties/type-name  channel-types/NumberT
                                                                   channel-properties/super-name channel-types/ChannelT
                                                                   channel-properties/fields     number-channel-fields/fields-list}))))
