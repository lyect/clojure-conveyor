(ns blocks.vertex.methods
  (:require [blocks.node.methods      :as node-methods]
            [blocks.vertex.exceptions :as vertex-exceptions]
            [blocks.vertex.properties :as vertex-properties]
            [utils]))


;; +-------------------------------+
;; |                               |
;; |   VERTEX RELATED PREDICATES   |
;; |                               |
;; +-------------------------------+

(defn vertex?
  "Predicate to check whether _obj_ is vertex or not"
  [obj]
  (and (some? obj)
       (map? obj)
       (contains? obj vertex-properties/node)
       (contains? obj vertex-properties/inputs-used)
       (let [node        (obj vertex-properties/node)
             inputs-used (obj vertex-properties/inputs-used)]
         (and
          (node-methods/node? node)
          (reduce #(and %1 (integer? (nth %2 0)) (boolean? (nth %2 1))) true inputs-used)
          (= (count inputs-used) (count (node-methods/get-node-inputs node)))))))

;; +-------------------------------------+
;; |                                     |
;; |   VERTEX RELATED PROPERTY GETTERS   |
;; |                                     |
;; +-------------------------------------+

;; No need to check whether vertex has the property or not. get-vertex-property is a private function,
;; hence it is used only for specific properties
(defn- get-vertex-property
  "Get property's value of _vertex_ by _property-name_"
  [vertex property-name]
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct vertex-exceptions/get-vertex-property vertex-exceptions/not-vertex
                                      (str "\"" vertex "\" is not a vertex"))))
  (vertex property-name))

;; No need to check whether "vertex" is a correct vertex or not
;; It will be done inside "get-vertex-property"
(defn get-vertex-node          [vertex] (get-vertex-property vertex vertex-properties/node))
(defn get-vertex-inputs-used   [vertex] (get-vertex-property vertex vertex-properties/inputs-used))
(defn get-vertex-outputs-used  [vertex] (get-vertex-property vertex vertex-properties/outputs-used))
(defn get-vertex-inputs-ready  [vertex] (get-vertex-property vertex vertex-properties/inputs-ready))

;; +--------------------+
;; |                    |
;; |   VERTEX METHODS   |
;; |                    |
;; +--------------------+

(defn get-input
  "Get _vertex_' input under _input-index_"
  [vertex input-index]
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct vertex-exceptions/get-input vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (nth (node-methods/get-node-inputs (get-vertex-node vertex)) input-index))

(defn get-output
  "Get _vertex_' output under _output-index_"
  [vertex output-index]
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct vertex-exceptions/get-output vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (nth (node-methods/get-node-outputs (get-vertex-node vertex)) output-index))

(defn inputs-count
  "Get number of inputs in _vertex_"
  [vertex]
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct vertex-exceptions/get-inputs-count vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (count (node-methods/get-node-inputs (get-vertex-node vertex))))

(defn outputs-count
  "Get number of outputs in _vertex_"
  [vertex]
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct vertex-exceptions/get-outputs-count vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (count (node-methods/get-node-outputs (get-vertex-node vertex))))

;; +----------------------------------------------+
;; |                                              |
;; |   VERTEX INPUTS AND OUTPUTS RELATED MACROS   |
;; |                                              |
;; +----------------------------------------------+

(defmacro ^:private set-io-property
  [io-properties-property ; Property of vertex
   io-properties-name     ; Name of list of io-properties
   io-properties-getter   ; Getter for io-properties
   exceptions-type
   vertex
   io-property-index]     ; Index of property in io-properties
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (let [io-properties (io-properties-getter vertex)]
    (when (true? (nth io-properties io-property-index))
      (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/already-set
                                          (str "Value of \"" io-properties-name "\" under \"" io-property-index "\" in \"" vertex "\" is already set"))))
     (alter vertex #(assoc % io-properties-property (assoc io-properties io-property-index true)))))

(defmacro ^:private reset-io-property
  [io-properties-property ; Property of vertex
   io-properties-name     ; Name of list of io-properties
   io-properties-getter   ; Getter for io-properties
   exceptions-type
   vertex
   io-property-index]     ; Index of property in io-properties
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (let [io-properties (io-properties-getter vertex)]
    (when (false? (nth io-properties io-property-index))
      (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/already-reset
                                          (str "Value of \"" io-properties-name "\" under \"" io-property-index "\" in \"" vertex "\" is already reset"))))
     (alter vertex #(assoc % io-properties-property (assoc io-properties io-property-index false)))))

(defmacro ^:private io-property-set?
  "Return usage of _vertex_' input under _input-index_"
  [io-properties-getter ; Getter for io-properties
   exceptions-type
   vertex
   io-property-index]   ; Index of property in io-properties
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (let [io-properties (io-properties-getter vertex)]
    (nth io-properties io-property-index)))

(defmacro ^:private all-io-properties-set?
  [io-properties-getter ; Getter for io-properties
   exceptions-type
   vertex]              ; Index of property in io-properties
  (when-not (vertex? vertex)
    (throw (vertex-exceptions/construct exceptions-type vertex-exceptions/not-vertex
                                        (str "\"" vertex "\" is not a vertex"))))
  (reduce and true (vals (io-properties-getter vertex))))

;; +---------------------------------+
;; |                                 |
;; |   VERTEX INPUTS USAGE METHODS   |
;; |                                 |
;; +---------------------------------+

(defn set-input-used
  "Set usage of _vertex_' input under _input-index_ to true"
  [vertex input-index]
  (set-io-property vertex-properties/inputs-used
                   "inputs-used"
                   get-vertex-inputs-used
                   vertex-exceptions/set-input-used
                   vertex
                   input-index))

(defn reset-input-used
  "Set usage of _vertex_' input under _input-index_ to false"
  [vertex input-index]
  (reset-io-property vertex-properties/inputs-used
                     "inputs-used"
                     get-vertex-inputs-used
                     vertex-exceptions/reset-input-used
                     vertex
                     input-index))

(defn input-used?
  "Return usage of _vertex_' input under _input-index_"
  [vertex input-index]
  (io-property-set? get-vertex-inputs-used
                    vertex-exceptions/input-used
                    vertex
                    input-index))

(defn all-inputs-used?
  "Return usage of _vertex_' inputs"
  [vertex]
  (all-io-properties-set? get-vertex-inputs-used
                          vertex-exceptions/all-inputs-used
                          vertex))

;; +----------------------------------+
;; |                                  |
;; |   VERTEX OUTPUTS USAGE METHODS   |
;; |                                  |
;; +----------------------------------+

(defn set-output-used
  "Set usage of _vertex_' output under _output-index_ to true"
  [vertex output-index]
  (set-io-property vertex-properties/outputs-used
                   "outputs-used"
                   get-vertex-outputs-used
                   vertex-exceptions/set-output-used
                   vertex
                   output-index))

(defn reset-output-used
  "Set usage of _vertex_' output under _output-index_ to false"
  [vertex output-index]
  (reset-io-property vertex-properties/outputs-used
                     "outputs-used"
                     get-vertex-outputs-used
                     vertex-exceptions/reset-output-used
                     vertex
                     output-index))

(defn output-used?
  "Return usage of _vertex_' output under _output-index_"
  [vertex output-index]
  (io-property-set? get-vertex-outputs-used
                    vertex-exceptions/output-used
                    vertex
                    output-index))

(defn all-outputs-used?
  "Return usage of _vertex_' outputs"
  [vertex]
  (all-io-properties-set? get-vertex-outputs-used
                          vertex-exceptions/all-outputs-used
                          vertex))

;; +-------------------------------------+
;; |                                     |
;; |   VERTEX INPUTS READINESS METHODS   |
;; |                                     |
;; +-------------------------------------+

(defn set-input-ready
  "Set readiness of _vertex_' input under _input-index_ to true"
  [vertex input-index]
  (set-io-property vertex-properties/inputs-ready
                   "inputs-ready"
                   get-vertex-inputs-ready
                   vertex-exceptions/set-input-ready
                   vertex
                   input-index))

(defn reset-input-ready
  "Set readiness of _vertex_' input under _input-index_ to false"
  [vertex input-index]
  (reset-io-property vertex-properties/inputs-ready
                     "inputs-ready"
                     get-vertex-inputs-ready
                     vertex-exceptions/reset-input-ready
                     vertex
                     input-index))

(defn input-ready?
  "Return readiness of _vertex_' input under _input-index_"
  [vertex input-index]
  (io-property-set? get-vertex-inputs-ready
                    vertex-exceptions/input-ready
                    vertex
                    input-index))

(defn all-inputs-ready?
  "Return readiness of _vertex_' inputs"
  [vertex]
  (all-io-properties-set? get-vertex-inputs-ready
                          vertex-exceptions/all-inputs-ready
                          vertex))

;; +------------------------+
;; |                        |
;; |   VERTEX CONSTRUCTOR   |
;; |                        |
;; +------------------------+

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-inputs-used
  "Transforms _node_'s inputs to map such as {0 false 1 false ... (<n inputs> - 1) false}"
  [node]
  (apply hash-map (reduce concat () (map (fn [idx] (list idx false)) (range (count (node-methods/get-node-inputs node)))))))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-outputs-used
  "Transforms _node_'s outputs to map such as {0 false 1 false ... (<n outputs> - 1) false}"
  [node]
  (apply hash-map (reduce concat () (map (fn [idx] (list idx false)) (range (count (node-methods/get-node-outputs node)))))))

;; No need to check whether "node" is correct node or not, since it is evaluated within "create" function
(defn- initialize-inputs-ready
  "Transforms _node_'s inputs to map such as {0 false 1 false ... (<n inputs> - 1) false}"
  [node]
  (apply hash-map (reduce concat () (map (fn [idx] (list idx false)) (range (count (node-methods/get-node-inputs node)))))))

(defn create
  "Create vertex from _node_"
  [node]
  (when-not (node-methods/node? node)
    (throw (vertex-exceptions/construct vertex-exceptions/create vertex-exceptions/not-node
                                        (str "\"" node "\" is not a node"))))
  (let [vertex (ref {})]
    (alter vertex #(assoc % vertex-properties/node         node))
    (alter vertex #(assoc % vertex-properties/inputs-used  (initialize-inputs-used node)))
    (alter vertex #(assoc % vertex-properties/outputs-used (initialize-outputs-used node)))
    (alter vertex #(assoc % vertex-properties/inputs-ready (initialize-inputs-ready node)))
    (when-not (utils/lists-equal? vertex-properties/properties-list (keys @vertex))
      (throw (vertex-exceptions/construct vertex-exceptions/create vertex-exceptions/vertex-properties-missing
                                          "Not all vertex-properties are added to create function")))))
