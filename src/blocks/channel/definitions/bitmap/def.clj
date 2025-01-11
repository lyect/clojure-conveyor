(ns blocks.channel.definitions.bitmap.def
  (:require [blocks.channel.definitions.bitmap.fields :as bitmap-channel-fields]
            [blocks.channel.definitions.image.def     :as image-channel-def]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define []
  (when-not (channel-types/defined? channel-types/BitmapT)
    (image-channel-def/define)
    (channel-types/define "Bitmap" channel-types/BitmapT
                          channel-properties/super-type-tag channel-types/ImageT
                          channel-properties/fields-tags    bitmap-channel-fields/tags-list)))
