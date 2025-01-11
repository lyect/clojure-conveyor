(ns blocks.node.input.methods
  (:require [blocks.channel.types         :as channel-types]
            [blocks.node.input.exceptions :as node-input-exceptions]
            [blocks.node.input.properties :as node-input-properties]
            [utils]))


;; +-----------------------------------+
;; |                                   |
;; |   NODE INPUT RELATED PREDICATES   |
;; |                                   |
;; +-----------------------------------+

(def input?
  (memoize
   (fn
     [obj]
     (and (utils/not-ref? obj)
          (map? obj)
          (some? (obj node-input-properties/tag))
          (some? (obj node-input-properties/channel-type))))))

;; +-----------------------------------------+
;; |                                         |
;; |   NODE INPUT RELATED PROPERTY GETTERS   |
;; |                                         |
;; +-----------------------------------------+

(defn- get-property
  [input property]
  (when-not (input? input)
    (throw (node-input-exceptions/construct node-input-exceptions/get-property node-input-exceptions/not-input
                                            (str "\"" input "\" is not a node input"))))
  (input property))

(def get-tag          (memoize (fn [input] (get-property input node-input-properties/tag))))
(def get-channel-type (memoize (fn [input] (get-property input node-input-properties/channel-type))))

;; +----------------------------+
;; |                            |
;; |   NODE INPUT CONSTRUCTOR   |
;; |                            |
;; +----------------------------+

(defn create
  [tag channel-type]
  (when-not (channel-types/declared? channel-type)
    (throw (node-input-exceptions/construct node-input-exceptions/create node-input-exceptions/type-undeclared
                                            (str "Channel type named \"" channel-type "\" is undeclared"))))
  (hash-map node-input-properties/tag          tag
            node-input-properties/channel-type channel-type))
