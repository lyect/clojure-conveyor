(ns blocks.channel.definitions.float.def
  (:require [blocks.channel.base                     :as channel-base]
            [blocks.channel.definitions.float.fields :as float-channel-fields]
            [blocks.channel.definitions.number.def   :as number-channel-def]
            [blocks.channel.properties               :as channel-properties]
            [blocks.channel.types                    :as channel-types]))


(defn define-float-channel []
  (when-not (channel-types/defined? channel-types/FloatT)
    (number-channel-def/define-number-channel)
    (channel-base/define-channel-type channel-types/FloatT
                                      channel-properties/super-name channel-types/NumberT
                                      channel-properties/fields float-channel-fields/fields-list)))
