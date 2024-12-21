(ns blocks.node.definitions.sharpen.def
  (:require [blocks.channel.definitions.image.def     :as image-channel-def]
            [blocks.channel.definitions.matrix.def    :as matrix-channel-def]
            [blocks.channel.definitions.matrix.fields :as matrix-channel-fields]
            [blocks.channel.methods                   :as channel-methods]
            [blocks.channel.types                     :as channel-types]
            [blocks.node.base                         :as node-base]
            [blocks.node.definitions.node.fields      :as base-node-fields]
            [blocks.node.definitions.sharpen.fields   :as sharpen-node-fields]
            [blocks.node.definitions.node.def         :as base-node-def]
            [blocks.node.methods                      :as node-methods]
            [blocks.node.properties                   :as node-properties]
            [blocks.node.types                        :as node-types]))

(defn- sharpen-node-function
  [node-ref]
  (let [i-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  0)
        k-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  1)
        output-buffer-ref  (nth (node-ref base-node-fields/output-buffers) 0)
        image              (first @i-input-buffer-ref)
        kernel             (first @k-input-buffer-ref)
        kernel-width       (channel-methods/get-channel-field @kernel matrix-channel-fields/width)
        kernel-height      (channel-methods/get-channel-field @kernel matrix-channel-fields/height)]
    (alter i-input-buffer-ref #(rest %))
    (alter k-input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Sharpen done with kernel (width: " kernel-width ", height: " kernel-height ")"))
    (alter output-buffer-ref #(conj % @image))))

(defn define-sharpen-node []
  (when-not (node-types/defined? node-types/SharpenT)
    (base-node-def/define-base-node)
    (image-channel-def/define-image-channel)
    (matrix-channel-def/define-matrix-channel)
    (node-base/define-node-type node-types/SharpenT
      node-properties/inputs   [channel-types/ImageT
                                channel-types/MatrixT]
      node-properties/outputs  [channel-types/ImageT]
      node-properties/function sharpen-node-function
      node-properties/fields   sharpen-node-fields/fields-list)))
