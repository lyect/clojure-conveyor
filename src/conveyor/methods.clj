(ns conveyor.methods
  (:require [blocks.edge.methods   :as edge-methods]
            [blocks.vertex.methods :as vertex-methods]
            [conveyor.exceptions   :as conveyor-exceptions]
            [conveyor.properties   :as conveyor-properties]
            [utils]))


;; +--------------------------+
;; |                          |
;; |   CONVEYOR CONSTRUCTOR   |
;; |                          |
;; +--------------------------+

(defn- initialize-vertices
  "Initialize conveyor's vertices"
  [nodes]
  (vector (map vertex-methods/create nodes)))

(defn- initialize-edges-map
  "Initialize conveyor's edges map"
  [vertices edges]
  (let [edges-map (ref {})]
    (doseq [edge edges]
      (let [begin-vertex-index        (edge-methods/get-begin-vertex-index        edge)
            begin-vertex-output-index (edge-methods/get-begin-vertex-output-index edge)
            end-vertex-index          (edge-methods/get-end-vertex-index          edge)
            end-vertex-input-index    (edge-methods/get-end-vertex-input-index    edge)
            begin-vertex              (nth vertices begin-vertex-index)
            end-vertex                (nth vertices end-vertex-index)
            begin-vertex-output       (vertex-methods/get-output begin-vertex begin-vertex-output-index)
            end-vertex-input          (vertex-methods/get-input  end-vertex   end-vertex-input-index)]
        (when-not (= begin-vertex-output end-vertex-input)
          (throw (conveyor-exceptions/construct conveyor-exceptions/create conveyor-exceptions/different-input-output
                                                (str "Output of \"" begin-vertex "\" differs from input of \"" end-vertex "\" for edge \"" edge "\""))))
        (vertex-methods/set-output-used begin-vertex begin-vertex-output-index)
        (vertex-methods/set-input-used  end-vertex   end-vertex-input-index)
        (alter edges-map #(assoc % [begin-vertex-index begin-vertex-output-index] [end-vertex-index end-vertex-input-index]))))))

(defn- initialize-inputs
  "Initialize conveyor's inputs"
  [vertices]
  (reduce
   (fn [conveyor-inputs [vertex-index vertex]]
     (if-not (vertex-methods/all-inputs-used? vertex)
       (doseq [input-index (range (vertex-methods/inputs-count vertex))]
         (when-not (vertex-methods/input-used? vertex input-index)
           (conj conveyor-inputs [vertex-index input-index])))
       conveyor-inputs))
   []
   (keep-indexed vector vertices)))
  
(defn- initialize-outputs
  "Initialize conveyor's outputs"
  [vertices]
  (reduce
   (fn [conveyor-outputs [vertex-index vertex]]
     (if-not (vertex-methods/all-outputs-used? vertex)
       (doseq [output-index (range (vertex-methods/outputs-count vertex))]
         (when-not (vertex-methods/output-used? vertex output-index)
           (conj conveyor-outputs [vertex-index output-index])))
       conveyor-outputs))
   []
   (keep-indexed vector vertices)))

(defn create
  "Create conveyor from _nodes_ and _edges_"
  [nodes edges]
  (let [conveyor  (ref {})
        vertices  (initialize-vertices nodes)
        edges-map (initialize-edges-map vertices edges)
        inputs    (initialize-inputs vertices)
        outputs   (initialize-outputs vertices)]
    (alter conveyor #(assoc % conveyor-properties/vertices vertices))
    (alter conveyor #(assoc % conveyor-properties/edges    edges-map))
    (alter conveyor #(assoc % conveyor-properties/inputs   inputs))
    (alter conveyor #(assoc % conveyor-properties/outputs  outputs))
    (when-not (utils/lists-equal? conveyor-properties/properties-list (keys @conveyor))
      (throw (conveyor-exceptions/construct conveyor-exceptions/create conveyor-exceptions/conveyor-properties-missing
                                          "Not all conveyor-properties are added to create function")))))
