(ns blocks.channel.definitions.integer.def
  (:require [blocks.channel.definitions.integer.fields :as integer-channel-fields]
            [blocks.channel.definitions.number.def     :as number-channel-def]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/IntegerT)
    (number-channel-def/define)
    (channel-types/define "Integer" channel-types/IntegerT
                          channel-properties/super-type-tag channel-types/NumberT
                          channel-properties/fields-tags    integer-channel-fields/tags-list)))
