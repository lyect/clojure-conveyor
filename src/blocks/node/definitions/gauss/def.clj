(ns blocks.node.definitions.gauss.def
  (:require [blocks.channel.types                 :as channel-types]
            [blocks.node.base                     :as node-base]
            [blocks.node.definitions.gauss.fields :as gauss-node-fields]
            [blocks.node.methods                  :as node-methods]
            [blocks.node.properties               :as node-properties]
            [blocks.node.types                    :as node-types]))


(defn- gauss-node-function
  "Apply gaussian blur filter to the _bitmap_ with sigma=_sigma_"
  [node bitmap sigma]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was blurred using gaussian blur with sigma=" sigma))
  bitmap)

(defn define-gauss-node []
  (when-not (node-types/defined? node-types/GaussT)
    (node-base/define-node-type node-types/GaussT
      node-properties/inputs   [channel-types/BitmapT
                                channel-types/FloatT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function gauss-node-function
      node-properties/fields   gauss-node-fields/fields-list)))
