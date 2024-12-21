(ns blocks.node.definitions.concat.def
  (:require [blocks.channel.definitions.bitmap.def  :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.base                        :as node-base]
            [blocks.node.definitions.node.fields     :as base-node-fields]
            [blocks.node.definitions.concat.fields   :as concat-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.methods                     :as node-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))

(defn- concat-node-function
  [node-ref]
  (let [node-fields            (node-ref node-properties/fields)
        input-buffer-ref       (nth (node-ref base-node-fields/input-buffers)  0)
        output-buffer-ref      (nth (node-ref base-node-fields/output-buffers) 0)
        [sum-width max-height] (reduce
                                (fn [[sum-width max-height] image]
                                  [(+   sum-width  (channel-methods/get-channel-field @image image-channel-fields/width))
                                   (max max-height (channel-methods/get-channel-field @image image-channel-fields/height))])
                                [0 0]
                                (subvec @input-buffer-ref 0 (node-fields concat-node-fields/n)))
        bitmap                 (channel-methods/create channel-types/BitmapT
                                                       image-channel-fields/width  sum-width
                                                       image-channel-fields/height max-height)]
    (println (str "[" (node-methods/get-node-name node-ref) "]: Concat result: Bitmap(widht: " sum-width ", height: " max-height ")"))
    (alter input-buffer-ref  #(subvec % (node-fields concat-node-fields/n)))
    (alter output-buffer-ref #(conj % bitmap))))

(defn define-concat-node []
  (when-not (node-types/defined? node-types/ConcatT)
    (base-node-def/define-base-node)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/ConcatT
                                node-properties/inputs   [channel-types/ImageT]
                                node-properties/outputs  [channel-types/ImageT]
                                node-properties/function concat-node-function
                                node-properties/fields   concat-node-fields/fields-list)))
