(ns blocks.channel.definitions.png.def
  (:require [blocks.channel.base                   :as channel-base]
            [blocks.channel.definitions.png.fields :as png-channel-fields]
            [blocks.channel.methods                :as channel-methods]
            [blocks.channel.properties             :as channel-properties]
            [blocks.channel.types                  :as channel-types]))


(defn define-png-channel []
  (when-not (channel-methods/defined? channel-types/PNG)
    (channel-base/define-channel-type channel-types/PNG
                                      channel-properties/fields png-channel-fields/fields-list)))
