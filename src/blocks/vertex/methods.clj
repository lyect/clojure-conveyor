(ns blocks.vertex.methods
  (:require [blocks.node.methods      :as    node-methods]
            [blocks.node.types        :as    node-types]
            [blocks.vertex.exceptions :as    vertex-exceptions]
            [blocks.vertex.properties :as    vertex-properties]
            [clojure.core.async       :as    a
                                      :refer [>!]]
            [utils]))


;; +-------------------------------+
;; |                               |
;; |   VERTEX RELATED PREDICATES   |
;; |                               |
;; +-------------------------------+

(def vertex?
  (memoize
   (fn [obj-ref]
     (and (some? @obj-ref)
          (map? @obj-ref)
          (obj-ref vertex-properties/node)
          (node-methods/node? (obj-ref vertex-properties/node))))))

;; +-------------------------------------+
;; |                                     |
;; |   VERTEX RELATED PROPERTY GETTERS   |
;; |                                     |
;; +-------------------------------------+

;; No need to check whether vertex has the property or not. get-vertex-property is a private function,
;; hence it is used only for specific properties
(defn- get-property
  [vertex-ref property-name]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-vertex-property vertex-exceptions/not-vertex
                                      (str "\"" vertex-ref "\" is not a vertex"))))
  (vertex-ref property-name))

;; No need to check whether "vertex" is a correct vertex or not
;; It will be done inside "get-vertex-property"
(def get-node   (memoize (fn [vertex-ref] (get-property vertex-ref vertex-properties/node))))
(def get-input  (memoize (fn [vertex-ref] (get-property vertex-ref vertex-properties/input))))
(def get-output (memoize (fn [vertex-ref] (get-property vertex-ref vertex-properties/output))))

;; +------------------------+
;; |                        |
;; |   VERTEX CONSTRUCTOR   |
;; |                        |
;; +------------------------+

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-node-inputs-connectivity
  [node-ref]
  (reduce #(assoc %1 %2 false) {} (node-types/get-inputs-tags (node-methods/get-type-tag node-ref))))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-node-outputs-connectivity
  [node-ref]
  (reduce #(assoc %1 %2 false) {} (node-types/get-outputs-tags (node-methods/get-type-tag node-ref))))

(defn create
  [node-ref]
  (when-not (node-methods/node? node-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/create vertex-exceptions/not-node
                                        (str "\"" node-ref "\" is not a node"))))
  (ref {vertex-properties/node                      node-ref
        vertex-properties/input                     (a/chan)
        vertex-properties/output                    (a/chan)
        vertex-properties/node-inputs-connectivity  (initialize-node-inputs-connectivity  node-ref)
        vertex-properties/node-outputs-connectivity (initialize-node-outputs-connectivity node-ref)}))

;; +----------------------------------------------+
;; |                                              |
;; |   VERTEX INPUTS AND OUTPUTS RELATED MACROS   |
;; |                                              |
;; +----------------------------------------------+

(defmacro ^:private set-io-property
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref
   io-property-tag]     ; Tag of property in io-properties
  `(when-not (vertex? ~vertex-ref)
     (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                         (str "\"" ~vertex-ref "\" is not a vertex"))))
  `(let [io-properties# (~vertex-ref ~io-properties-property)]
     (alter ~vertex-ref #(assoc % ~io-properties-property (assoc io-properties# ~io-property-tag true)))))

(defmacro ^:private io-property-set?
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref
   io-property-tag]     ; Tag of property in io-properties
  `(when-not (vertex? ~vertex-ref)
     (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                         (str "\"" ~vertex-ref "\" is not a vertex"))))
  `((~vertex-ref ~io-properties-property) ~io-property-tag))

(defmacro ^:private all-io-properties-set?
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref]
  `(when-not (vertex? ~vertex-ref)
    (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" ~vertex-ref "\" is not a vertex"))))
  `(every? true? (vals (~vertex-ref ~io-properties-property))))

;; +---------------------------------+
;; |                                 |
;; |   VERTEX INPUTS USAGE METHODS   |
;; |                                 |
;; +---------------------------------+

(defn set-node-input-connected
  [vertex-ref node-input-tag]
  (set-io-property vertex-properties/node-inputs-connectivity
                   vertex-exceptions/set-node-input-connected
                   vertex-ref
                   node-input-tag))

(defn node-input-connected?
  [vertex-ref node-input-tag]
  (io-property-set? vertex-properties/node-inputs-connectivity
                    vertex-exceptions/node-input-connected
                    vertex-ref
                    node-input-tag))

(defn all-node-inputs-connected?
  [vertex-ref]
  (all-io-properties-set? vertex-properties/node-inputs-connectivity
                          vertex-exceptions/all-node-inputs-connected
                          vertex-ref))

;; +----------------------------------+
;; |                                  |
;; |   VERTEX OUTPUTS USAGE METHODS   |
;; |                                  |
;; +----------------------------------+

(defn set-node-output-connected
  [vertex-ref node-output-tag]
  (set-io-property vertex-properties/node-outputs-connectivity
                   vertex-exceptions/set-node-output-connected
                   vertex-ref
                   node-output-tag))

(defn node-output-connected?
  [vertex-ref node-output-tag]
  (io-property-set? vertex-properties/node-outputs-connectivity
                    vertex-exceptions/node-output-connected
                    vertex-ref
                    node-output-tag))

(defn all-node-outputs-connected?
  [vertex-ref]
  (all-io-properties-set? vertex-properties/node-outputs-connectivity
                          vertex-exceptions/all-node-outputs-connected
                          vertex-ref))

;; +--------------------+
;; |                    |
;; |   VERTEX METHODS   |
;; |                    |
;; +--------------------+

(def get-node-input-channel-type
  (memoize
   (fn [vertex-ref node-input-tag]
     (when-not (vertex? vertex-ref)
       (throw (vertex-exceptions/construct vertex-exceptions/get-node-input-channel-type vertex-exceptions/not-vertex
                                          (str "\"" vertex-ref "\" is not a vertex"))))
     (node-types/get-input-channel-type (node-methods/get-type-tag (get-node vertex-ref)) node-input-tag))))

(def get-node-output-channel-type
  (memoize
   (fn [vertex-ref node-output-tag]
     (when-not (vertex? vertex-ref)
       (throw (vertex-exceptions/construct vertex-exceptions/get-node-output-channel-type vertex-exceptions/not-vertex
                                           (str "\"" vertex-ref "\" is not a vertex"))))
     (node-types/get-output-channel-type (node-methods/get-type-tag (get-node vertex-ref)) node-output-tag))))

(def get-node-inputs-tags
  (memoize
   (fn [vertex-ref]
     (when-not (vertex? vertex-ref)
       (throw (vertex-exceptions/construct vertex-exceptions/get-node-inputs-count vertex-exceptions/not-vertex
                                           (str "\"" vertex-ref "\" is not a vertex"))))
     (node-types/get-inputs-tags (node-methods/get-type-tag (get-node vertex-ref))))))

(def get-node-outputs-tags
  (memoize
   (fn [vertex-ref]
     (when-not (vertex? vertex-ref)
       (throw (vertex-exceptions/construct vertex-exceptions/get-node-outputs-count vertex-exceptions/not-vertex
                                           (str "\"" vertex-ref "\" is not a vertex"))))
     (node-types/get-outputs-tags (node-methods/get-type-tag (get-node vertex-ref))))))

(defn- run
  [vertex-ref]
  (a/thread (while true
              (a/alt!! (vertex-ref vertex-properties/input)
                       ([[node-input-tag value]]
                        (let [node-ref       (get-node vertex-ref)
                              execute-status (dosync (node-methods/store node-ref node-input-tag value)
                                                     (node-methods/execute node-ref))]
                          (when execute-status
                            (doseq [node-output-tag (node-types/get-outputs-tags (node-methods/get-type-tag node-ref))]
                              (doseq [output-value (node-methods/flush-output node-ref node-output-tag)]
                                (a/go (>! (vertex-ref vertex-properties/output) [node-output-tag output-value])))))))))))

(defn start
  [vertex-ref]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/start vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (run vertex-ref))
