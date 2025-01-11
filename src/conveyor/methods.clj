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
  [obj-ref]
  (and (some? @obj-ref)
       (map? @obj-ref)
       (obj-ref conveyor-properties/vertices)
       (obj-ref conveyor-properties/edges-map)
       (obj-ref conveyor-properties/outputs-buffers)))

;; +--------------------------+
;; |                          |
;; |   CONVEYOR CONSTRUCTOR   |
;; |                          |
;; +--------------------------+

(defn- initialize-edges-map
  [vertices-refs edges-list]
    (reduce
     #(let [begin-vertex-index                    (edge-methods/get-begin-vertex-index           %2)
            begin-vertex-node-output-tag          (edge-methods/get-begin-vertex-node-output-tag %2)
            end-vertex-index                      (edge-methods/get-end-vertex-index             %2)
            end-vertex-node-input-tag             (edge-methods/get-end-vertex-node-input-tag    %2)
            begin-vertex                          (nth vertices-refs begin-vertex-index)
            end-vertex                            (nth vertices-refs end-vertex-index)
            begin-vertex-node-output-channel-type (vertex-methods/get-node-output-channel-type begin-vertex begin-vertex-node-output-tag)
            end-vertex-node-input-channel-type    (vertex-methods/get-node-input-channel-type  end-vertex   end-vertex-node-input-tag)]
         (when-not (channel-types/subtype? begin-vertex-node-output-channel-type end-vertex-node-input-channel-type)
           (throw (conveyor-exceptions/construct conveyor-exceptions/create conveyor-exceptions/different-input-output
                                                 (str "Output channel type of \"" begin-vertex-node-output-channel-type "\" differs from input channel type of \"" end-vertex-node-input-channel-type "\" for edge \"" %2 "\""))))
         (vertex-methods/set-node-output-connected begin-vertex begin-vertex-node-output-tag)
         (vertex-methods/set-node-input-connected  end-vertex   end-vertex-node-input-tag)
         (assoc %1 [begin-vertex-index begin-vertex-node-output-tag] [end-vertex-index end-vertex-node-input-tag]))
     {}
     edges-list))

(defn- initialize-outputs-buffers
  [vertices-refs]
  (reduce
   (fn [outputs-buffers [vertex-index vertex-ref]]
     (if-not (vertex-methods/all-node-outputs-connected? vertex-ref)
       (into outputs-buffers
             (->> (vertex-methods/get-node-outputs-tags vertex-ref)
                  (filter #(not (vertex-methods/node-output-connected? vertex-ref %)))
                  (reduce #(assoc %1 [vertex-index %2] (ref [])) {})))
       outputs-buffers))
   {}
   (keep-indexed vector vertices-refs)))

(defn create
  [nodes-refs edges]
  (let [conveyor-ref  (ref {})
        vertices-refs (map vertex-methods/create nodes-refs)]
    
    (alter conveyor-ref #(assoc % conveyor-properties/vertices        vertices-refs))
    (alter conveyor-ref #(assoc % conveyor-properties/edges-map       (initialize-edges-map vertices-refs edges)))
    (alter conveyor-ref #(assoc % conveyor-properties/outputs-buffers (initialize-outputs-buffers vertices-refs)))
    conveyor-ref))

;; +----------------------+
;; |                      |
;; |   CONVEYOR METHODS   |
;; |                      |
;; +----------------------+

(defn- initialize-output2vertex-map
   [conv-ref]
     (reduce
      (fn [m vertex-ref] (assoc m (vertex-methods/get-output vertex-ref) vertex-ref))
      {}
      (conv-ref conveyor-properties/vertices)))

(defn- run
  [conv-ref]
  (let [vertices (conv-ref conveyor-properties/vertices)
        output2vertex-map (initialize-output2vertex-map conv-ref)]
    (doall (map vertex-methods/start vertices))
    (a/go
      (while true
        (let [[[node-output-tag value] output-ch] (a/alts! (keys output2vertex-map))
              vertex-producer-ref   (output2vertex-map output-ch)
              vertex-producer-index (.indexOf vertices vertex-producer-ref)
              output-buffer-ref     ((conv-ref conveyor-properties/outputs-buffers) [vertex-producer-index node-output-tag])]
          (if (some? output-buffer-ref)
            (dosync
             (alter output-buffer-ref #(conj % value)))
            (let [[vertex-consumer-index node-input-tag] ((conv-ref conveyor-properties/edges-map) [vertex-producer-index node-output-tag])
                  vertex-consumer-ref (nth vertices vertex-consumer-index)]
              (>! (vertex-methods/get-input vertex-consumer-ref) [node-input-tag value]))))))))

(defn start
  [conv-ref]
  (when-not (conveyor? conv-ref)
    (throw (conveyor-exceptions/construct conveyor-exceptions/start conveyor-exceptions/not-conveyor
                                          (str "\"" conv-ref "\" is not a conveyor"))))
  (run conv-ref))

(defn store
  [conv-ref store-info]
  (when-not (conveyor? conv-ref)
    (throw (conveyor-exceptions/construct conveyor-exceptions/store conveyor-exceptions/not-conveyor
                                          (str "\"" conv-ref "\" is not a conveyor"))))
  (a/go
   (doseq [[[vertex-index node-input-tag] value] store-info]
     (let [vertex-ref (nth (conv-ref conveyor-properties/vertices) vertex-index)]
       (when-not (utils/in-list? (vertex-methods/get-node-inputs-tags vertex-ref) node-input-tag)
         (throw (conveyor-exceptions/construct conveyor-exceptions/store conveyor-exceptions/not-conveyor-input
                                               (str "Input tagged \"" node-input-tag "\" of vertex indexed \"" vertex-index "\" is not a conveyor input"))))
       (>! (vertex-methods/get-input vertex-ref) [node-input-tag value])))))

(defn flush-output
  [conv-ref vertex-index node-output-tag]
  (when-not (conveyor? conv-ref)
    (throw (conveyor-exceptions/construct conveyor-exceptions/flush-output conveyor-exceptions/not-conveyor
                                          (str "\"" conv-ref "\" is not a conveyor"))))
  (let [output-buffer-ref ((conv-ref conveyor-properties/outputs-buffers) [vertex-index node-output-tag])]
    (when-not (some? output-buffer-ref)
      (throw (conveyor-exceptions/construct conveyor-exceptions/flush-output conveyor-exceptions/not-conveyor-output
                                            (str "Output tagged \"" node-output-tag "\" of vertex indexed \"" vertex-index "\" is not a conveyor output"))))
    (let [output-buffer @output-buffer-ref]
      (dosync (ref-set output-buffer-ref []))
      output-buffer)))
