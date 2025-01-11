(ns blocks.channel.definitions.float.def
  (:require [blocks.channel.definitions.float.fields :as float-channel-fields]
            [blocks.channel.definitions.number.def   :as number-channel-def]
            [blocks.channel.properties               :as channel-properties]
            [blocks.channel.types                    :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/FloatT)
    (number-channel-def/define)
    (channel-types/define "Float" channel-types/FloatT
                          channel-properties/super-type-tag channel-types/NumberT
                          channel-properties/fields-tags    float-channel-fields/tags-list)))
