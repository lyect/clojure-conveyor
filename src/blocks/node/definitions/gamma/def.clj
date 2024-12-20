(ns blocks.node.definitions.gamma.def
  (:require [blocks.channel.definitions.image.def :as image-channel-def]
            [blocks.channel.definitions.number.def :as number-channel-def]
            [blocks.channel.methods               :as channel-methods]
            [blocks.channel.types                 :as channel-types]
            [blocks.node.base                     :as node-base]
            [blocks.node.definitions.node.fields  :as base-node-fields]
            [blocks.node.definitions.gamma.fields :as gamma-node-fields]
            [blocks.node.definitions.node.def     :as base-node-def]
            [blocks.node.methods                  :as node-methods]
            [blocks.node.properties               :as node-properties]
            [blocks.node.types                    :as node-types]))

(defn- gamma-ready-validator
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        i-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)
        g-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 1)]
    (and (seq? @i-input-buffer-ref) (seq? @g-input-buffer-ref))))

(defn- gamma-inputs-validator
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        i-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)
        g-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 1)]
    (and (channel-types/subtype? (channel-methods/get-channel-type-name (first @i-input-buffer-ref)) channel-types/ImageT)
         (channel-types/subtype? (channel-methods/get-channel-type-name (first @g-input-buffer-ref)) channel-types/NumberT))))

(defn- gamma-node-function
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        i-input-buffer-ref (get (node-fields base-node-fields/input-buffers)  0)
        g-input-buffer-ref (get (node-fields base-node-fields/input-buffers)  1)
        output-buffer-ref  (get (node-fields base-node-fields/output-buffers) 0)
        image              (first @i-input-buffer-ref)
        gamma              (first @g-input-buffer-ref)]
    (alter i-input-buffer-ref #(rest @i-input-buffer-ref))
    (alter g-input-buffer-ref #(rest @g-input-buffer-ref))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Gamma correction done with gamma=" gamma))
    (alter output-buffer-ref #(conj % image))))

(defn define-gamma-node []
  (when-not (node-types/defined? node-types/GammaT)
    (base-node-def/define-base-node)
    (image-channel-def/define-image-channel)
    (number-channel-def/define-number-channel)
    (node-base/define-node-type node-types/GammaT
      node-properties/inputs           [channel-types/ImageT
                                        channel-types/NumberT]
      node-properties/outputs          [channel-types/ImageT]
      node-properties/function         gamma-node-function
      node-properties/ready-validator  gamma-ready-validator
      node-properties/inputs-validator gamma-inputs-validator
      node-properties/fields           gamma-node-fields/fields-list)))
