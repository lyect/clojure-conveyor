(ns blocks.channel.definitions.float.def
  (:require [blocks.channel.base                     :as channel-base]
            [blocks.channel.definitions.float.fields :as float-channel-fields]
            [blocks.channel.properties               :as channel-properties]
            [blocks.channel.types                    :as channel-types]))


(defn define-float-channel []
  (when-not (channel-types/defined? channel-types/FloatT)
    (channel-base/define-channel-type channel-types/FloatT
                                      channel-properties/fields float-channel-fields/fields-list)))
