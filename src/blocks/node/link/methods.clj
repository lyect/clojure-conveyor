(ns blocks.node.link.methods
  (:require [blocks.node.link.exceptions :as node-link-exceptions]
            [blocks.node.link.properties :as node-link-properties]
            [utils]))


;; +----------------------------------+
;; |                                  |
;; |   NODE LINK RELATED PREDICATES   |
;; |                                  |
;; +----------------------------------+

(def input?
  (memoize
   (fn
     [obj]
     (and (utils/not-ref? obj)
          (map? obj)
          (some? (obj node-link-properties/inputs-tags))
          (some? (obj node-link-properties/outputs-tags))
          (some? (obj node-link-properties/handler))))))

;; +----------------------------------------+
;; |                                        |
;; |   NODE LINK RELATED PROPERTY GETTERS   |
;; |                                        |
;; +----------------------------------------+

(defn- get-property
  [link property]
  (when-not (input? link)
    (throw (node-link-exceptions/construct node-link-exceptions/get-property node-link-exceptions/not-link
                                           (str "\"" link "\" is not a node input"))))
  (link property))

(def get-inputs-tags  (memoize (fn [link] (get-property link node-link-properties/inputs-tags))))
(def get-outputs-tags (memoize (fn [link] (get-property link node-link-properties/outputs-tags))))
(def get-handler      (memoize (fn [link] (get-property link node-link-properties/handler))))

;; +---------------------------+
;; |                           |
;; |   NODE LINK CONSTRUCTOR   |
;; |                           |
;; +---------------------------+

(defn create
  [inputs-tags outputs-tags handler]
  (when-not (vector? inputs-tags)
    (throw (node-link-exceptions/construct node-link-exceptions/create node-link-exceptions/inputs-tags-not-vector
                                           (str "Given inputs tags is not a vector"))))
  (when-not (vector? outputs-tags)
    (throw (node-link-exceptions/construct node-link-exceptions/create node-link-exceptions/outputs-tags-not-vector
                                           (str "Given outputs tags is not a vector"))))
  (when-not (fn? handler)
    (throw (node-link-exceptions/construct node-link-exceptions/create node-link-exceptions/handler-not-function
                                           (str "Given handler is not a function"))))
  (hash-map node-link-properties/inputs-tags  inputs-tags
            node-link-properties/outputs-tags outputs-tags
            node-link-properties/handler      handler))
