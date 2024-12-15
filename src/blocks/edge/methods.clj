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
  "Predicate to check whether _obj_ is edge or not"
  [obj]
  (and (some? obj)
       (map? obj)
       (contains? obj edge-properties/begin-vertex-index)
       (contains? obj edge-properties/begin-vertex-output-index)
       (contains? obj edge-properties/end-vertex-index)
       (contains? obj edge-properties/end-vertex-input-index)
       (let [bvi  (obj edge-properties/begin-vertex-index)
             bvoi (obj edge-properties/begin-vertex-output-index)
             evi  (obj edge-properties/end-vertex-index)
             evii (obj edge-properties/end-vertex-input-index)]
         (and
          (integer? bvi)  (<= 0 bvi)
          (integer? bvoi) (<= 0 bvoi)
          (integer? evi)  (<= 0 evi)
          (integer? evii) (<= 0 evii)))))

;; +-----------------------------------+
;; |                                   |
;; |   EDGE RELATED PROPERTY GETTERS   |
;; |                                   |
;; +-----------------------------------+

;; No need to check whether edge has the property or not. get-edge-property is a private function,
;; hence it is used only for specific properties
(defn- get-edge-property
  "Get property's value of _edge_ by _property-name_"
  [edge property-name]
  (when-not (edge? edge)
    (throw (edge-exceptions/construct edge-exceptions/get-edge-property edge-exceptions/not-edge
                                      (str "\"" edge "\" is not an edge"))))
  (edge property-name))

;; No need to check whether "edge" is a correct edge or not
;; It will be done inside "get-edge-property"
(defn get-begin-vertex-index        [edge] (get-edge-property edge edge-properties/begin-vertex-index))
(defn get-begin-vertex-output-index [edge] (get-edge-property edge edge-properties/begin-vertex-output-index))
(defn get-end-vertex-index          [edge] (get-edge-property edge edge-properties/end-vertex-index))
(defn get-end-vertex-input-index    [edge] (get-edge-property edge edge-properties/end-vertex-input-index))

;; +----------------------+
;; |                      |
;; |   EDGE CONSTRUCTOR   |
;; |                      |
;; +----------------------+

(defn create
  "Create edge from (_begin-vertex-index_, _begin-vertex-output-index_) to (_end-vertex-index_, _end-vertex-input-index_)"
  [begin-vertex-index begin-vertex-output-index end-vertex-index end-vertex-input-index]
  (when-not (integer? begin-vertex-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/begin-vertex-index-not-integer
                                      "Begin vertex index is not integer")))
  (when-not (integer? begin-vertex-output-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/begin-vertex-output-index-not-integer
                                      "Begin vertex output index is not integer")))
  (when-not (integer? end-vertex-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/end-vertex-index-not-integer
                                      "Begin vertex index is not integer")))
  (when-not (integer? end-vertex-input-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/end-vertex-input-index-not-integer
                                      "Begin vertex input index is not integer")))
  (when-not (<= 0 begin-vertex-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/begin-vertex-index-negative
                                      "Begin vertex index is negative")))
  (when-not (<= 0 begin-vertex-output-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/begin-vertex-output-index-negative
                                      "Begin vertex output index is negative")))
  (when-not (<= 0 end-vertex-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/end-vertex-index-negative
                                      "Begin vertex index is not negative")))
  (when-not (<= 0 end-vertex-input-index)
    (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/end-vertex-input-index-negative
                                      "Begin vertex input index is negative")))
  (let [edge (hash-map edge-properties/begin-vertex-index        begin-vertex-index
                       edge-properties/begin-vertex-output-index begin-vertex-output-index
                       edge-properties/end-vertex-index          end-vertex-index
                       edge-properties/end-vertex-input-index    end-vertex-input-index)]
    (when-not (utils/lists-equal? edge-properties/properties-list (keys edge))
        (throw (edge-exceptions/construct edge-exceptions/create edge-exceptions/edge-properties-missing
                                          "Not all edge-properties are added to create function")))))
