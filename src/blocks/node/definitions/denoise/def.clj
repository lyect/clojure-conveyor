(ns blocks.node.definitions.denoise.def
  (:require [blocks.channel.definitions.image.def   :as image-channel-def]
            [blocks.channel.methods                 :as channel-methods]
            [blocks.channel.types                   :as channel-types]
            [blocks.node.base                       :as node-base]
            [blocks.node.definitions.node.fields    :as base-node-fields]
            [blocks.node.definitions.denoise.fields :as denoise-node-fields]
            [blocks.node.definitions.node.def       :as base-node-def]
            [blocks.node.methods                    :as node-methods]
            [blocks.node.properties                 :as node-properties]
            [blocks.node.types                      :as node-types]))

(defn- denoise-ready-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (seq? @input-buffer-ref)))

(defn- denoise-inputs-validator
  [node-ref]
  (let [node-fields      (node-ref node-properties/fields)
        input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)]
    (channel-types/subtype? (channel-methods/get-channel-type-name (first @input-buffer-ref)) channel-types/ImageT)))

(defn- denoise-node-function
  [node-ref]
  (let [node-fields       (node-ref node-properties/fields)
        input-buffer-ref  (get (node-fields base-node-fields/input-buffers)  0)
        output-buffer-ref (get (node-fields base-node-fields/output-buffers) 0)
        image             (first @input-buffer-ref)]
    (alter input-buffer-ref #(rest @input-buffer-ref))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Denoise done"))
    (alter output-buffer-ref #(conj % image))))

(defn define-denoise-node []
  (when-not (node-types/defined? node-types/DenoiseT)
    (base-node-def/define-base-node)
    (image-channel-def/define-image-channel)
    (node-base/define-node-type node-types/DenoiseT
                                node-properties/inputs           [channel-types/ImageT]
                                node-properties/outputs          [channel-types/ImageT]
                                node-properties/function         denoise-node-function
                                node-properties/ready-validator  denoise-ready-validator
                                node-properties/inputs-validator denoise-inputs-validator
                                node-properties/fields           denoise-node-fields/fields-list)))
