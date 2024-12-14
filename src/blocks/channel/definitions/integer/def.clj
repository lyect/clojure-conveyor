(ns blocks.channel.definitions.integer.def
  (:require [blocks.channel.base                       :as channel-base]
            [blocks.channel.definitions.integer.fields :as integer-channel-fields]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]))


(defn define-integer-channel []
  (when-not (channel-types/defined? channel-types/IntegerT)
    (channel-base/define-channel-type channel-types/IntegerT
                                      channel-properties/fields integer-channel-fields/fields-list)))
