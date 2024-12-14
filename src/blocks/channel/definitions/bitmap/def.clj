(ns blocks.channel.definitions.bitmap.def
  (:require [blocks.channel.base                      :as channel-base]
            [blocks.channel.definitions.bitmap.fields :as bitmap-channel-fields]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-bitmap-channel []
  (when-not (channel-types/defined? channel-types/BitmapT)
    (channel-base/define-channel-type channel-types/BitmapT
                                      channel-properties/fields bitmap-channel-fields/fields-list)))
