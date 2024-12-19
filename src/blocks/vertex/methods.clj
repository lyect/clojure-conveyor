(ns blocks.vertex.methods
  (:require [blocks.node.methods      :as    node-methods]
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

(defn vertex?
  "Predicate to check whether _obj_ is vertex or not"
  [obj-ref]
  (and (some? @obj-ref)
       (map? @obj-ref)
       (obj-ref vertex-properties/node)
       (node-methods/node? (obj-ref vertex-properties/node))))

;; +-------------------------------------+
;; |                                     |
;; |   VERTEX RELATED PROPERTY GETTERS   |
;; |                                     |
;; +-------------------------------------+

;; No need to check whether vertex has the property or not. get-vertex-property is a private function,
;; hence it is used only for specific properties
(defn- get-vertex-property
  "Get property's value of _vertex_ by _property-name_"
  [vertex-ref property-name]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-vertex-property vertex-exceptions/not-vertex
                                      (str "\"" vertex-ref "\" is not a vertex"))))
  (vertex-ref property-name))

;; No need to check whether "vertex" is a correct vertex or not
;; It will be done inside "get-vertex-property"
(defn get-vertex-node    [vertex-ref] (get-vertex-property vertex-ref vertex-properties/node))
(defn get-vertex-inputs  [vertex-ref] (get-vertex-property vertex-ref vertex-properties/inputs))
(defn get-vertex-outputs [vertex-ref] (get-vertex-property vertex-ref vertex-properties/outputs))

;; +------------------------+
;; |                        |
;; |   VERTEX CONSTRUCTOR   |
;; |                        |
;; +------------------------+

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-inputs
  "Get async channel for each _node_'s input"
  [node-ref]
  (into [] (repeat (count (node-methods/get-node-inputs node-ref)) a/chan)))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-outputs
  "Get async channel for each _node_'s input"
  [node-ref]
  (into [] (repeat (count (node-methods/get-node-outputs node-ref)) a/chan)))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-inputs-connectivity
  "Get connectivity status for each _node_'s input"
  [node-ref]
  (into [] (repeat (count (node-methods/get-node-inputs node-ref)) false)))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-outputs-connectivity
  "Get connectivity status for each _node_'s output"
  [node-ref]
  (into [] (repeat (count (node-methods/get-node-outputs node-ref)) false)))

(defn create
  "Create vertex from _node_"
  [node-ref]
  (when-not (node-methods/node? node-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/create vertex-exceptions/not-node
                                        (str "\"" node-ref "\" is not a node"))))
  (let [vertex-ref (ref {})]
    (alter vertex-ref #(assoc % vertex-properties/node                 node-ref))
    (alter vertex-ref #(assoc % vertex-properties/inputs               (initialize-inputs  node-ref)))
    (alter vertex-ref #(assoc % vertex-properties/outputs              (initialize-outputs node-ref)))
    (alter vertex-ref #(assoc % vertex-properties/inputs-connectivity  (initialize-inputs-connectivity  node-ref)))
    (alter vertex-ref #(assoc % vertex-properties/outputs-connectivity (initialize-outputs-connectivity node-ref)))
    (when-not (utils/lists-equal? vertex-properties/properties-list (keys @vertex-ref))
      (throw (vertex-exceptions/construct vertex-exceptions/create vertex-exceptions/vertex-properties-missing
                                          "Not all vertex-properties are added to create function")))
    vertex-ref))

;; +----------------------------------------------+
;; |                                              |
;; |   VERTEX INPUTS AND OUTPUTS RELATED MACROS   |
;; |                                              |
;; +----------------------------------------------+

(defmacro ^:private set-io-property
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref
   io-property-index]     ; Index of property in io-properties
  `(when-not (vertex? ~vertex-ref)
     (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                         (str "\"" ~vertex-ref "\" is not a vertex"))))
  `(let [io-properties# (~vertex-ref ~io-properties-property)]
     (alter ~vertex-ref #(assoc % ~io-properties-property (assoc io-properties# ~io-property-index true)))))

(defmacro ^:private io-property-set?
  "Return usage of _vertex_' input under _input-index_"
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref
   io-property-index]     ; Index of property in io-properties
  `(when-not (vertex? ~vertex-ref)
     (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                         (str "\"" ~vertex-ref "\" is not a vertex"))))
  `(let [io-properties# (~vertex-ref ~io-properties-property)]
     (nth io-properties# ~io-property-index)))

(defmacro ^:private all-io-properties-set?
  [io-properties-property ; Property of vertex
   exceptions-type
   vertex-ref]                ; Index of property in io-properties]
  `(when-not (vertex? ~vertex-ref)
    (throw (vertex-exceptions/construct ~exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" ~vertex-ref "\" is not a vertex"))))
  `(reduce 'and true (~vertex-ref ~io-properties-property)))

;; +---------------------------------+
;; |                                 |
;; |   VERTEX INPUTS USAGE METHODS   |
;; |                                 |
;; +---------------------------------+

(defn set-input-connected
  "Set connectivity of _vertex_' input under _input-index_ to true"
  [vertex-ref input-index]
  (set-io-property vertex-properties/inputs-connectivity
                   vertex-exceptions/set-input-connected
                   vertex-ref
                   input-index))

(defn input-connected?
  "Return connectivity of _vertex_' input under _input-index_"
  [vertex-ref input-index]
  (io-property-set? vertex-properties/inputs-connectivity
                    vertex-exceptions/input-connected
                    vertex-ref
                    input-index))

(defn all-inputs-connected?
  "Return connectivity of _vertex_' inputs"
  [vertex-ref]
  (all-io-properties-set? vertex-properties/inputs-connectivity
                          vertex-exceptions/all-inputs-connected
                          vertex-ref))

;; +----------------------------------+
;; |                                  |
;; |   VERTEX OUTPUTS USAGE METHODS   |
;; |                                  |
;; +----------------------------------+

(defn set-output-connected
  "Set connectivity of _vertex_' output under _output-index_ to true"
  [vertex-ref output-index]
  (set-io-property vertex-properties/outputs-connectivity
                   vertex-exceptions/set-output-connected
                   vertex-ref
                   output-index))

(defn output-connected?
  "Return connectivity of _vertex_' output under _output-index_"
  [vertex-ref output-index]
  (io-property-set? vertex-properties/outputs-connectivity
                    vertex-exceptions/output-connected
                    vertex-ref
                    output-index))

(defn all-outputs-connected?
  "Return connectivity of _vertex_' outputs"
  [vertex-ref]
  (all-io-properties-set? vertex-properties/outputs-connectivity
                          vertex-exceptions/all-outputs-connected
                          vertex-ref))

;; +--------------------+
;; |                    |
;; |   VERTEX METHODS   |
;; |                    |
;; +--------------------+

(defn get-node-input
  "Get _vertex_' node input under _input-index_"
  [vertex-ref input-index]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-node-input vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (nth (node-methods/get-node-inputs (get-vertex-node vertex-ref)) input-index))

(defn get-node-output
  "Get _vertex_' node output under _output-index_"
  [vertex-ref output-index]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-node-output vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (nth (node-methods/get-node-outputs (get-vertex-node vertex-ref)) output-index))

(defn get-node-inputs-count
  "Get number of inputs in _vertex_"
  [vertex-ref]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-node-inputs-count vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (count (node-methods/get-node-inputs (get-vertex-node vertex-ref))))

(defn get-node-outputs-count
  "Get number of outputs in _vertex_"
  [vertex-ref]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/get-node-outputs-count vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (count (node-methods/get-node-outputs (get-vertex-node vertex-ref))))

(defn- run
  [vertex-ref]
  (a/go (while true
          (let [[value ch]  (a/alts! (get-vertex-inputs vertex-ref))
                input-index (.indexOf (get-vertex-inputs vertex-ref) ch)]
            (when (= input-index -1)
              (throw (vertex-exceptions/construct vertex-exceptions/run vertex-exceptions/unknown-channel
                                                  (str "Got value \"" value "\" from unknown channel \"" ch "\""))))
            (let [node-ref (get-vertex-node vertex-ref)]
              (dosync
               (node-methods/store   node-ref input-index value)
               (node-methods/execute node-ref)
               (doseq [output-index (range (count (node-methods/get-node-outputs node-ref)))]
                 (let [output (get (get-vertex-outputs vertex-ref) output-index)]
                   (doseq [output-value (->> (node-methods/flush-output node-ref output-index)
                                             (list)
                                             (flatten))]
                     (>! output output-value))))))))))

(defn start
  "Start async task for vertex"
  [vertex-ref]
  (when-not (vertex? vertex-ref)
    (throw (vertex-exceptions/construct vertex-exceptions/start vertex-exceptions/not-vertex
                                        (str "\"" vertex-ref "\" is not a vertex"))))
  (run vertex-ref))
