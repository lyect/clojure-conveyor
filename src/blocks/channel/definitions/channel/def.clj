(ns blocks.channel.definitions.channel.def
  (:require [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]))

(defn define-base-channel []
  (when-not (channel-types/defined? channel-types/ChannelT)
    (dosync
      (alter channel-hierarchy/tree #(assoc % channel-types/ChannelT {channel-properties/type-name  channel-types/ChannelT
                                                                      channel-properties/super-name nil
                                                                      channel-properties/fields     base-channel-fields/fields-list})))))
