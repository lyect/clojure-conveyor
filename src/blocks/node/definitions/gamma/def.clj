(ns blocks.node.definitions.gamma.def
  (:require [blocks.channel.definitions.image.def  :as image-channel-def]
            [blocks.channel.definitions.number.def :as number-channel-def]
            [blocks.channel.types                  :as channel-types]
            [blocks.node.base                      :as node-base]
            [blocks.node.definitions.node.fields   :as base-node-fields]
            [blocks.node.definitions.gamma.fields  :as gamma-node-fields]
            [blocks.node.definitions.node.def      :as base-node-def]
            [blocks.node.methods                   :as node-methods]
            [blocks.node.properties                :as node-properties]
            [blocks.node.types                     :as node-types]))

(defn- gamma-node-function
  [node-ref]
  (let [i-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  0)
        g-input-buffer-ref (nth (node-ref base-node-fields/input-buffers)  1)
        output-buffer-ref  (nth (node-ref base-node-fields/output-buffers) 0)
        image              (first @i-input-buffer-ref)
        gamma              (first @g-input-buffer-ref)]
    (alter i-input-buffer-ref #(rest %))
    (alter g-input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Gamma correction done with gamma=" gamma))
    (alter output-buffer-ref #(conj % image))))

(defn define-gamma-node []
  (when-not (node-types/defined? node-types/GammaT)
    (base-node-def/define-base-node)
    (image-channel-def/define-image-channel)
    (number-channel-def/define-number-channel)
    (node-base/define-node-type node-types/GammaT
      node-properties/inputs   [channel-types/ImageT
                                channel-types/NumberT]
      node-properties/outputs  [channel-types/ImageT]
      node-properties/function gamma-node-function
      node-properties/fields   gamma-node-fields/fields-list)))
