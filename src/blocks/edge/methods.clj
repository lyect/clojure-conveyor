(ns blocks.edge.methods
  (:require [blocks.edge.exceptions :as edge-exceptions]
            [blocks.edge.properties :as edge-properties]
            [utils]))


;; +-----------------------------+
;; |                             |
;; |   EDGE RELATED PREDICATES   |
;; |                             |
;; +-----------------------------+

(defn edge?
  [obj]
  (and (some? obj)
       (map? obj)
       (contains? obj edge-properties/begin-vertex-index)
       (contains? obj edge-properties/begin-vertex-node-output-tag)
       (contains? obj edge-properties/end-vertex-index)
       (contains? obj edge-properties/end-vertex-node-input-tag)))

;; +-----------------------------------+
;; |                                   |
;; |   EDGE RELATED PROPERTY GETTERS   |
;; |                                   |
;; +-----------------------------------+

;; No need to check whether edge has the property or not. get-edge-property is a private function,
;; hence it is used only for specific properties
(defn- get-property
  [edge property-name]
  (when-not (edge? edge)
    (throw (edge-exceptions/construct edge-exceptions/get-edge-property edge-exceptions/not-edge
                                      (str "\"" edge "\" is not an edge"))))
  (edge property-name))

;; No need to check whether "edge" is a correct edge or not
;; It will be done inside "get-edge-property"
(defn get-begin-vertex-index           [edge] (get-property edge edge-properties/begin-vertex-index))
(defn get-begin-vertex-node-output-tag [edge] (get-property edge edge-properties/begin-vertex-node-output-tag))
(defn get-end-vertex-index             [edge] (get-property edge edge-properties/end-vertex-index))
(defn get-end-vertex-node-input-tag    [edge] (get-property edge edge-properties/end-vertex-node-input-tag))

;; +----------------------+
;; |                      |
;; |   EDGE CONSTRUCTOR   |
;; |                      |
;; +----------------------+

(defn create
  [begin-vertex-index begin-vertex-node-output-tag end-vertex-index end-vertex-node-input-tag]
  (hash-map edge-properties/begin-vertex-index           begin-vertex-index
            edge-properties/begin-vertex-node-output-tag begin-vertex-node-output-tag
            edge-properties/end-vertex-index             end-vertex-index
            edge-properties/end-vertex-node-input-tag    end-vertex-node-input-tag))
