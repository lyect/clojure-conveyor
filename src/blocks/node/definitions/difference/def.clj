(ns blocks.node.definitions.difference.def
  (:require [blocks.channel.definitions.image.fields   :as image-channel-fields]
            [blocks.channel.methods                    :as channel-methods]
            [blocks.channel.types                      :as channel-types]
            [blocks.node.base                          :as node-base]
            [blocks.node.definitions.base.fields       :as base-node-fields]
            [blocks.node.definitions.difference.fields :as difference-node-fields]
            [blocks.node.definitions.node.def          :as base-node-def]
            [blocks.node.methods                       :as node-methods]
            [blocks.node.properties                    :as node-properties]
            [blocks.node.types                         :as node-types]))

(defn- difference-ready-validator
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        l-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)
        r-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 1)]
    (and (seq? @l-input-buffer-ref) (seq? @r-input-buffer-ref))))

(defn- difference-inputs-validator
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        l-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 0)
        r-input-buffer-ref (get (node-fields base-node-fields/input-buffers) 1)]
    (and (channel-types/subtype? (channel-methods/get-channel-type-name (first @l-input-buffer-ref)) channel-types/ImageT)
         (channel-types/subtype? (channel-methods/get-channel-type-name (first @r-input-buffer-ref)) channel-types/ImageT))))

(defn- difference-node-function
  [node-ref]
  (let [node-fields        (node-ref node-properties/fields)
        l-input-buffer-ref (get (node-fields base-node-fields/input-buffers)  0)
        r-input-buffer-ref (get (node-fields base-node-fields/input-buffers)  1)
        output-buffer-ref  (get (node-fields base-node-fields/output-buffers) 0)
        l-image            (first @l-input-buffer-ref)
        r-image            (first @r-input-buffer-ref)
        l-image-width      (channel-methods/get-channel-field l-image image-channel-fields/width)
        r-image-width      (channel-methods/get-channel-field r-image image-channel-fields/width)
        l-image-height     (channel-methods/get-channel-field l-image image-channel-fields/height)
        r-image-height     (channel-methods/get-channel-field r-image image-channel-fields/height)
        max-width          (max l-image-width r-image-width)
        max-height         (max l-image-height r-image-height)]
    (alter l-input-buffer-ref #(rest @l-input-buffer-ref))
    (alter r-input-buffer-ref #(rest @r-input-buffer-ref))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Difference done: (width: " max-width ", height: " max-height ")"))
    (alter output-buffer-ref
           #(conj % (channel-methods/create channel-types/BitmapT
                                            image-channel-fields/width  max-width
                                            image-channel-fields/height max-height)))))

(defn define-difference-node []
  (when-not (node-types/defined? node-types/DifferenceT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/DifferenceT
      node-properties/inputs           [channel-types/ImageT
                                        channel-types/ImageT]
      node-properties/outputs          [channel-types/ImageT]
      node-properties/function         difference-node-function
      node-properties/ready-validator  difference-ready-validator
      node-properties/inputs-validator difference-inputs-validator
      node-properties/fields           difference-node-fields/fields-list)))
