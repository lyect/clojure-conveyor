(ns blocks.node.output.methods
  (:require [blocks.channel.types          :as channel-types]
            [blocks.node.output.exceptions :as node-output-exceptions]
            [blocks.node.output.properties :as node-output-properties]
            [utils]))


;; +------------------------------------+
;; |                                    |
;; |   NODE OUTPUT RELATED PREDICATES   |
;; |                                    |
;; +------------------------------------+

(def output?
  (memoize
   (fn
     [obj]
     (and (utils/not-ref? obj)
          (map? obj)
          (some? (obj node-output-properties/tag))
          (some? (obj node-output-properties/channel-type))))))

;; +------------------------------------------+
;; |                                          |
;; |   NODE OUTPUT RELATED PROPERTY GETTERS   |
;; |                                          |
;; +------------------------------------------+

(defn- get-property
  [output property]
  (when-not (output? output)
    (throw (node-output-exceptions/construct node-output-exceptions/get-property node-output-exceptions/not-output
                                             (str "\"" output "\" is not a node output"))))
  (output property))

(def get-tag          (memoize (fn [output] (get-property output node-output-properties/tag))))
(def get-channel-type (memoize (fn [output] (get-property output node-output-properties/channel-type))))

;; +-----------------------------+
;; |                             |
;; |   NODE OUTPUT CONSTRUCTOR   |
;; |                             |
;; +-----------------------------+

(defn create
  [tag channel-type]
  (when-not (channel-types/declared? channel-type)
    (throw (node-output-exceptions/construct node-output-exceptions/create node-output-exceptions/type-undeclared
                                             (str "Channel type named \"" channel-type "\" is undeclared"))))
  (hash-map node-output-properties/tag          tag
            node-output-properties/channel-type channel-type))
