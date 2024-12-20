(ns conveyor.methods
  (:require [blocks.channel.types  :as channel-types]
            [blocks.edge.methods   :as edge-methods]
            [blocks.vertex.methods :as vertex-methods]
            [conveyor.exceptions   :as conveyor-exceptions]
            [conveyor.properties   :as conveyor-properties]
            [clojure.core.async    :as    a
                                   :refer [>!]]
            [utils]))


;; +---------------------------------+
;; |                                 |
;; |   CONVEYOR RELATED PREDICATES   |
;; |                                 |
;; +---------------------------------+

(defn conveyor?
  "Predicate to check whether _obj_ is conveyor or not"
  [obj-ref]
  (and (some? @obj-ref)
       (map? @obj-ref)
       (obj-ref conveyor-properties/vertices)
       (obj-ref conveyor-properties/edges)
       (obj-ref conveyor-properties/inputs)
       (obj-ref conveyor-properties/outputs)))

;; +---------------------------------------+
;; |                                       |
;; |   CONVEYOR RELATED PROPERTY GETTERS   |
;; |                                       |
;; +---------------------------------------+

;; No need to check whether conveyor has the property or not. get-conveyor-property is a private function,
;; hence it is used only for specific properties
(defn- get-conveyor-property
  "Get property's value of _conveyor_ by _property-name_"
  [conveyor-ref property-name]
  (when-not (conveyor? conveyor-ref)
    (throw (conveyor-exceptions/construct conveyor-exceptions/get-conveyor-property conveyor-exceptions/not-conveyor
                                      (str "\"" conveyor-ref "\" is not a conveyor: " @conveyor-ref))))
  (conveyor-ref property-name))

;; No need to check whether "conveyor" is a correct conveyor or not
;; It will be done inside "get-conveyor-property"
(defn get-conveyor-vertices [conveyor-ref] (get-conveyor-property conveyor-ref conveyor-properties/vertices))
(defn get-conveyor-edges    [conveyor-ref] (get-conveyor-property conveyor-ref conveyor-properties/edges))
(defn get-conveyor-inputs   [conveyor-ref] (get-conveyor-property conveyor-ref conveyor-properties/inputs))
(defn get-conveyor-outputs  [conveyor-ref] (get-conveyor-property conveyor-ref conveyor-properties/outputs))

;; +--------------------------+
;; |                          |
;; |   CONVEYOR CONSTRUCTOR   |
;; |                          |
;; +--------------------------+

(defn- initialize-vertices
  "Initialize conveyor's vertices"
  [nodes-refs]
  (into [] (map vertex-methods/create nodes-refs)))

(defn- initialize-edges-map
  "Initialize conveyor's edges map"
  [vertices-refs edges]
  (let [edges-map (ref {})]
    (doseq [edge edges]
      (let [begin-vertex-index        (edge-methods/get-begin-vertex-index        edge)
            begin-vertex-output-index (edge-methods/get-begin-vertex-output-index edge)
            end-vertex-index          (edge-methods/get-end-vertex-index          edge)
            end-vertex-input-index    (edge-methods/get-end-vertex-input-index    edge)
            begin-vertex              (nth vertices-refs begin-vertex-index)
            end-vertex                (nth vertices-refs end-vertex-index)
            begin-vertex-output       (vertex-methods/get-node-output begin-vertex begin-vertex-output-index)
            end-vertex-input          (vertex-methods/get-node-input  end-vertex   end-vertex-input-index)]
        (when-not (channel-types/subtype? begin-vertex-output end-vertex-input)
          (throw (conveyor-exceptions/construct conveyor-exceptions/create conveyor-exceptions/different-input-output
                                                (str "Output of \"" begin-vertex "\" differs from input of \"" end-vertex "\" for edge \"" edge "\""))))
        (vertex-methods/set-output-connected begin-vertex begin-vertex-output-index)
        (vertex-methods/set-input-connected  end-vertex   end-vertex-input-index)
        (alter edges-map #(assoc % [begin-vertex-index begin-vertex-output-index] [end-vertex-index end-vertex-input-index]))))
    edges-map))

(defn- initialize-inputs
  "Initialize conveyor's inputs"
  [vertices-refs]
  (reduce
   (fn [conveyor-inputs [vertex-index vertex-ref]]
     (if-not (vertex-methods/all-inputs-connected? vertex-ref)
       (into conveyor-inputs
             (map
              #(vector vertex-index %)
              (filter
               #(not (vertex-methods/input-connected? vertex-ref %))
               (range (vertex-methods/get-node-inputs-count vertex-ref)))))
        conveyor-inputs))
   []
   (keep-indexed vector vertices-refs)))

(defn- initialize-outputs
  "Initialize conveyor's outputs"
  [vertices-refs]
  (reduce
   (fn [conveyor-outputs [vertex-index vertex-ref]]
     (if-not (vertex-methods/all-outputs-connected? vertex-ref)
       (into conveyor-outputs
             (map
              #(vector vertex-index %)
              (filter
               #(not (vertex-methods/output-connected? vertex-ref %))
               (range (vertex-methods/get-node-outputs-count vertex-ref)))))
       conveyor-outputs))
   []
   (keep-indexed vector vertices-refs)))

(defn create
  "Create conveyor from _nodes_ and _edges_"
  [nodes-refs edges]
  (let [conveyor-ref  (ref {})
        vertices-refs (initialize-vertices nodes-refs)
        edges-map     (initialize-edges-map vertices-refs edges)
        inputs        (initialize-inputs vertices-refs)
        outputs       (initialize-outputs vertices-refs)]
    (alter conveyor-ref #(assoc % conveyor-properties/vertices vertices-refs))
    (alter conveyor-ref #(assoc % conveyor-properties/edges    edges-map))
    (alter conveyor-ref #(assoc % conveyor-properties/inputs   inputs))
    (alter conveyor-ref #(assoc % conveyor-properties/outputs  outputs))
    (when-not (utils/lists-equal? conveyor-properties/properties-list (keys @conveyor-ref))
      (throw (conveyor-exceptions/construct conveyor-exceptions/create conveyor-exceptions/conveyor-properties-missing
                                          "Not all conveyor-properties are added to create function")))
    conveyor-ref))

;; +--------------------------+
;; |                          |
;; |   CONVEYOR METHODS       |
;; |                          |
;; +--------------------------+

(defn- set-input-params
  [conv-ref input-params]
  (a/go
    (doseq [[[vertex-index input-index] value] input-params]
      (let [vertex (nth (get-conveyor-vertices conv-ref) vertex-index)
            input  (nth (vertex-methods/get-vertex-inputs vertex) input-index)]
        (>! input value)))))

(defn- get-outputs-map
  [conv-ref]
  (let [vertices (get-conveyor-vertices conv-ref)]
    (reduce (fn [m v] (reduce #(assoc %1 %2 v) m (vertex-methods/get-vertex-outputs v))) {} vertices)))

(defn- listen-outputs
  [conv-ref]
  (let [outputs (map (fn [[vertex-index output-index]]
                       (let [vertex (nth (get-conveyor-vertices conv-ref) vertex-index)]
                         (nth (vertex-methods/get-vertex-outputs vertex) output-index)))
                     (get-conveyor-outputs conv-ref))]
    (a/go
      (while true
        (let [[value output] (a/alts! outputs)]
          (println (str "From " output " produced value: " value)))))))

(defn- run
  [conv-ref]
  (let [vertices (get-conveyor-vertices conv-ref)
        outputs-map (get-outputs-map conv-ref)]
   (map vertex-methods/start vertices)
   (-> (Thread. (fn [] (listen-outputs conv-ref))) .start)
   (a/go
     (while true
       (let [[value output-ch] (a/alts! (keys outputs-map))
             vertex-ref (outputs-map output-ch)
             vertex-index (.indexOf vertices vertex-ref)
             output-index (.indexOf (vertex-methods/get-vertex-outputs vertex-ref) output-ch)]
         (when (= vertex-index -1)
           (throw (conveyor-exceptions/construct conveyor-exceptions/run conveyor-exceptions/unknown-vertex
                                                 (str "Work with unknown vertex \"" vertex-ref "\""))))
         (when (= output-index -1)
           (throw (conveyor-exceptions/construct conveyor-exceptions/run conveyor-exceptions/unknown-channel
                                                 (str "Got value \"" value "\" from unknown channel \"" output-ch "\""))))
         (let [edge ((get-conveyor-edges conv-ref) [vertex-index output-index])
               vertex-consumer-ref (nth (get-conveyor-vertices conv-ref) (first edge))
               vertex-consumer-input (nth (vertex-methods/get-vertex-inputs vertex-consumer-ref) (second edge))]
           (>! vertex-consumer-input value)))))))

(defn start
  "Start _conv-ref_ with _input-params_: <[vertex input-channel-index] value>"
  [conv-ref input-params-map]
  (when-not (conveyor? conv-ref)
    (throw (conveyor-exceptions/construct conveyor-exceptions/start conveyor-exceptions/not-conveyor
                                          (str "\"" conv-ref "\" is not a conveyor"))))
  (when-not (utils/lists-equal? (keys input-params-map) (get-conveyor-inputs conv-ref))
    (throw (conveyor-exceptions/construct conveyor-exceptions/start conveyor-exceptions/not-all-input-params
                                          (str "Not all input params define for \"" conv-ref "\""))))
  (set-input-params conv-ref input-params-map)
  (run conv-ref))
