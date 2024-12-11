(ns blocks.channel.definitions.bitmap.def
  (:require [blocks.channel.base                      :as channel-base]
            [blocks.channel.definitions.bitmap.fields :as bitmap-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-bitmap-channel []
  (when-not (channel-methods/defined? channel-types/Bitmap)
    (channel-base/define-channel-type channel-types/Bitmap
                                      channel-properties/fields bitmap-channel-fields/fields-list)))
