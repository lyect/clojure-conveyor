(ns blocks.node.definitions.cut.def
  (:require [blocks.channel.definitions.bitmap.def   :as bitmap-channel-def]
            [blocks.channel.definitions.image.fields :as image-channel-fields]
            [blocks.channel.methods                  :as channel-methods]
            [blocks.channel.types                    :as channel-types]
            [blocks.node.base                        :as node-base]
            [blocks.node.definitions.node.fields     :as base-node-fields]
            [blocks.node.definitions.cut.fields      :as cut-node-fields]
            [blocks.node.definitions.node.def        :as base-node-def]
            [blocks.node.methods                     :as node-methods]
            [blocks.node.properties                  :as node-properties]
            [blocks.node.types                       :as node-types]))

(defn- cut-node-function
  [node-ref]
  (let [node-fields       (node-ref node-properties/fields)
        input-buffer-ref  (nth (node-ref base-node-fields/input-buffers)  0)
        output-buffer-ref (nth (node-ref base-node-fields/output-buffers) 0)
        image             (first @input-buffer-ref)
        new-width         (quot (channel-methods/get-channel-field @image image-channel-fields/width) (node-fields cut-node-fields/n))
        new-height        (channel-methods/get-channel-field @image image-channel-fields/height)]
    (alter input-buffer-ref #(rest %))
    (println (str "[" (node-methods/get-node-name node-ref) "]: Cut result: N Bitmaps such as (width: " new-width ", height: " new-height ")"))
    (alter output-buffer-ref #(into % (repeat
                                       (node-fields cut-node-fields/n)
                                       (channel-methods/create channel-types/BitmapT
                                                               image-channel-fields/width  new-width
                                                               image-channel-fields/height new-height))))))

(defn define-cut-node []
  (when-not (node-types/defined? node-types/CutT)
    (base-node-def/define-base-node)
    (bitmap-channel-def/define-bitmap-channel)
    (node-base/define-node-type node-types/CutT
                                node-properties/inputs   [channel-types/ImageT]
                                node-properties/outputs  [channel-types/ImageT]
                                node-properties/function cut-node-function
                                node-properties/fields   cut-node-fields/fields-list)))
