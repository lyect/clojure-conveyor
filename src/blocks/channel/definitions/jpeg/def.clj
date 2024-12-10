(ns blocks.channel.definitions.jpeg.def
  (:require [blocks.channel.base                    :as channel-base]
            [blocks.channel.definitions.jpeg.fields :as jpeg-channel-fields]
            [blocks.channel.methods                 :as channel-methods]
            [blocks.channel.properties              :as channel-properties]
            [blocks.channel.types                   :as channel-types]))
  
  
(defn define-jpeg-channel []
  (when-not (channel-methods/channel-type-defined? channel-types/JPEG)
    (channel-base/define-channel-type channel-types/JPEG
                                      channel-properties/fields jpeg-channel-fields/field-list)))