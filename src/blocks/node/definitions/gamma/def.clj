(ns blocks.node.definitions.gamma.def
  (:require [blocks.channel.types                 :as channel-types]
            [blocks.node.base                     :as node-base]
            [blocks.node.definitions.gamma.fields :as gamma-node-fields]
            [blocks.node.methods                  :as node-methods]
            [blocks.node.properties               :as node-properties]
            [blocks.node.types                    :as node-types]))


(defn- gamma-node-function
  "Apply gamma correction to the _bitmap_ with gamma=_gamma_"
  [node bitmap gamma]
  (println (str "[" (node-methods/get-node-name node) "]: " bitmap " was gamma-corrected with gamma=" gamma))
  bitmap)

(defn define-gamma-node []
  (when-not (node-types/defined? node-types/GammaT)
    (base-node-def/define-base-node)
    (node-base/define-node-type node-types/GammaT
      node-properties/inputs   [channel-types/BitmapT
                                channel-types/FloatT]
      node-properties/outputs  [channel-types/BitmapT]
      node-properties/function gamma-node-function
      node-properties/fields   gamma-node-fields/fields-list)))
