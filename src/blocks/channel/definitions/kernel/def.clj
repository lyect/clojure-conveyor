(ns blocks.channel.definitions.kernel.def
  (:require [blocks.channel.base                      :as channel-base]
            [blocks.channel.definitions.kernel.fields :as kernel-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-kernel-channel []
  (when-not (channel-methods/channel-type-defined? channel-types/Kernel)
    (channel-base/define-channel-type channel-types/Kernel
                                      channel-properties/fields kernel-channel-fields/field-list)))