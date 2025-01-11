(ns blocks.channel.definitions.channel.def
  (:require [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/ChannelT)
    (dosync
      (alter channel-hierarchy/tree #(assoc % channel-types/ChannelT {channel-properties/super-type-tag nil
                                                                      channel-properties/label          "Channel"
                                                                      channel-properties/fields-tags    base-channel-fields/tags-list})))))
