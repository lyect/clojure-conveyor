(ns blocks.channel.definitions.matrix.def
  (:require [blocks.channel.base                      :as channel-base]
            [blocks.channel.definitions.matrix.fields :as matrix-channel-fields]
            [blocks.channel.properties                :as channel-properties]
            [blocks.channel.types                     :as channel-types]))


(defn define-matrix-channel []
  (when-not (channel-types/defined? channel-types/MatrixT)
    (channel-base/define-channel-type channel-types/MatrixT
                                      channel-properties/fields matrix-channel-fields/fields-list)))
