(ns blocks.node.methods
  (:require
   [blocks.node.definitions.node.fields :as base-node-fields]
   [blocks.node.exceptions              :as node-exceptions]
   [blocks.node.link.methods            :as node-link-methods]
   [blocks.node.types                   :as node-types]
   [clojure.set                         :as cljset]
   [utils]
   [blocks.node.methods :as node-methods]
   [blocks.node.hierarchy :as node-hierarchy]))


;; +---------------+
;; |               |
;; |   CONSTANTS   |
;; |               |
;; +---------------+

(def ^:private min-input-load 1)

;; +-----------------------------+
;; |                             |
;; |   NODE RELATED PREDICATES   |
;; |                             |
;; +-----------------------------+

(def node?
  (memoize
   (fn [obj-ref]
     (and (utils/ref? obj-ref)
          @obj-ref
          (map? @obj-ref)
          (reduce #(and %1 (some? (obj-ref %2))) true base-node-fields/tags-list)
          (let [type-tag (obj-ref base-node-fields/type-tag)]
            (and (node-types/defined? type-tag)
                 (utils/lists-equal?  (remove (set base-node-fields/tags-list) (keys @obj-ref))
                                      (node-types/get-fields-tags type-tag))))))))

;; +-----------------------------------+
;; |                                   |
;; |   NODE INSTANCE RELATED GETTERS   |
;; |                                   |
;; +-----------------------------------+

(defn get-field-value
  [node-ref field-tag]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-field-value node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (when-not ((apply dissoc @node-ref base-node-fields/tags-list) field-tag)
    (throw (node-exceptions/construct node-exceptions/get-field-value node-exceptions/unknown-field-tag
                                      (str "\"" node-ref "\" has no field tagged \"" field-tag "\""))))
  (node-ref field-tag))

(def get-type-tag
  (memoize
   (fn [node-ref]
     (when-not (node? node-ref)
       (throw (node-exceptions/construct node-exceptions/get-type-tag node-exceptions/not-node
                                         (str "\"" node-ref "\" is not a node"))))
     (node-ref base-node-fields/type-tag))))

(def get-label
  (memoize
   (fn [node-ref]
     (when-not (node? node-ref)
       (throw (node-exceptions/construct node-exceptions/get-label node-exceptions/not-node
                                         (str "\"" node-ref "\" is not a node"))))
     (node-ref base-node-fields/label))))

(defn get-fields
  [node-ref]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/get-fields node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (apply dissoc @node-ref base-node-fields/tags-list))

;; +----------------------+
;; |                      |
;; |   NODE CONSTRUCTOR   |
;; |                      |
;; +----------------------+

(defn create
  [label type-tag & fields]
  (when-not (node-types/declared? type-tag)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undeclared
                                      (str "Type tagged \"" type-tag "\" is undeclared"))))
  (when-not (node-types/defined? type-tag)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-undefined
                                      (str "Type tagged \"" type-tag "\" is undefined"))))
  (when (node-types/abstract? type-tag)
    (throw (node-exceptions/construct node-exceptions/create node-exceptions/type-abstract
                                      (str "Type tagged \"" type-tag "\" is abstract"))))
  (let [fields-map           (apply hash-map fields)
        fields-tags          (keys fields-map)
        fields-tags-set      (set fields-tags)
        type-fields-tags     (node-types/get-fields-tags type-tag)
        type-fields-tags-set (set type-fields-tags)
        node-ref             (ref {})]
    (when (> (count fields-tags) (count fields-tags-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/duplicated-fields-tags
                                        (str "Tried to create node of type tagged \"" type-tag "\" with duplicated fields tags"))))
    (when-not (empty? (cljset/difference type-fields-tags-set fields-tags-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/missed-fields-tags
                                        (str "Tried to create node of type tagged \"" type-tag "\" with missing fields tags"))))
    (when-not (empty? (cljset/difference fields-tags-set type-fields-tags-set))
      (throw (node-exceptions/construct node-exceptions/create node-exceptions/excess-fields-tags
                                        (str "Tried to create node of type tagged \"" type-tag "\" with excess fields tags"))))
    (doseq [fields-map-entry fields-map]
      (alter node-ref #(assoc % (first fields-map-entry) (second fields-map-entry))))
    (alter node-ref (fn [node] (assoc node base-node-fields/type-tag              type-tag)))
    (alter node-ref (fn [node] (assoc node base-node-fields/label                 label)))
    (alter node-ref (fn [node] (assoc node base-node-fields/inputs-buffers        (reduce #(assoc %1 %2 (ref []))       {} (node-types/get-inputs-tags  type-tag)))))
    (alter node-ref (fn [node] (assoc node base-node-fields/outputs-buffers       (reduce #(assoc %1 %2 (ref []))       {} (node-types/get-outputs-tags type-tag)))))
    (alter node-ref (fn [node] (assoc node base-node-fields/required-inputs-loads (reduce #(assoc %1 %2 min-input-load) {} (node-types/get-inputs-tags  type-tag)))))
    (node-hierarchy/reserve-label label)
    node-ref))

;; +------------------+
;; |                  |
;; |   NODE METHODS   |
;; |                  |
;; +------------------+

(defn set-required-input-load
  [node-ref input-tag required-load]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/set-required-input-load node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (when-not (utils/in-list? (node-types/get-inputs-tags (get-type-tag node-ref)) input-tag)
    (throw (node-exceptions/construct node-exceptions/set-required-input-load node-exceptions/unknown-input-tag
                                      (str "\"" node-ref "\" has no input tagged \"" input-tag "\""))))
  (when-not (<= min-input-load required-load)
    (throw (node-exceptions/construct node-exceptions/set-required-input-load node-exceptions/small-min-load
                                      (str "Given load is too small, must be >= " min-input-load))))
  (alter node-ref #(assoc % base-node-fields/required-inputs-loads
                           (assoc (node-ref base-node-fields/required-inputs-loads) input-tag required-load)))
  nil)

(def ^:private get-input-buffer-ref
  (memoize
   (fn [node-ref input-tag]
     ((node-ref base-node-fields/inputs-buffers) input-tag))))

(def ^:private get-output-buffer-ref
  (memoize
   (fn [node-ref output-tag]
     ((node-ref base-node-fields/outputs-buffers) output-tag))))

(defn store
  [node-ref input-tag value]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/store node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  
  (when (utils/in-list? (node-types/get-linked-inputs-tags (get-type-tag node-ref)) input-tag)
    (alter (get-input-buffer-ref node-ref input-tag) #(conj % value)))
  nil)

(defn flush-output
  [node-ref output-tag]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/flush-output node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (let [output-buffer-ref (get-output-buffer-ref node-ref output-tag)
        output-buffer     @output-buffer-ref]
    (dosync (ref-set output-buffer-ref []))
    output-buffer))

(defn- execute-for-link
  [node-ref link]
  (let [linked-inputs-tags (node-link-methods/get-inputs-tags link)]
   (loop [linked-inputs-buffers (map #(get-input-buffer-ref node-ref %) linked-inputs-tags)
          done-iteration        false]
     (let [linked-inputs-required-loads              (map #((node-ref base-node-fields/required-inputs-loads) %) linked-inputs-tags)
           linked-inputs-buffers-with-required-loads (utils/zip linked-inputs-buffers linked-inputs-required-loads)]
       (if (->> linked-inputs-buffers-with-required-loads
                (map (fn [[input-buffer-ref required-load]] (<= required-load (count @input-buffer-ref))))
                (every? true?))
         (do
           (doseq [[output-tag output-values] (apply (partial (node-link-methods/get-handler link) (node-methods/get-fields node-ref))
                                                     (map (fn [[input-buffer-ref required-load]]
                                                            (subvec @input-buffer-ref 0 required-load))
                                                          linked-inputs-buffers-with-required-loads))]
             (alter (get-output-buffer-ref node-ref output-tag) #(into % output-values)))
           (doseq [[input-buffer-ref required-load] linked-inputs-buffers-with-required-loads]
             (alter input-buffer-ref #(subvec % required-load)))
           (recur linked-inputs-buffers true))
         done-iteration)))))

(defn execute
  [node-ref]
  (when-not (node? node-ref)
    (throw (node-exceptions/construct node-exceptions/execute node-exceptions/not-node
                                      (str "\"" node-ref "\" is not a node"))))
  (->> node-ref
       get-type-tag
       node-types/get-links
       (map #(execute-for-link node-ref %))
       doall
       (some true?)))
