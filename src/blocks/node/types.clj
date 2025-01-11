(ns blocks.node.types
  (:require [clojure.set                         :as cljset]
            [blocks.node.definitions.node.fields :as base-node-fields]
            [blocks.node.exceptions              :as node-exceptions]
            [blocks.node.hierarchy               :as node-hierarchy]
            [blocks.node.input.methods           :as node-input-methods]
            [blocks.node.link.methods            :as node-link-methods]
            [blocks.node.output.methods          :as node-output-methods]
            [blocks.node.properties              :as node-properties]
            [utils]))

;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def NodeT ::type-Node)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def ConcatT      ::type-Concat)
(def CutT         ::type-Cut)
(def DenoiseT     ::type-Denoise)
(def DifferenceT  ::type-Difference)
(def GammaT       ::type-Gamma)
(def Image2ImageT ::type-Image2Image)
(def RGBSplitT    ::type-RGBSplit)
(def SharpenT     ::type-Sharpen)

(def ^:private types-tags-list [; Base type
                                NodeT
                                ; Derived types
                                ConcatT
                                CutT
                                DenoiseT
                                DifferenceT
                                GammaT
                                Image2ImageT
                                RGBSplitT
                                SharpenT])

(def ^:private abstract-types-tags-list [])

;; +----------------------------------+
;; |                                  |
;; |   NODE TYPE RELATED PREDICATES   |
;; |                                  |
;; +----------------------------------+

(defn declared?
  [type-tag]
  (utils/in-list? types-tags-list type-tag))

(defn defined?
  [type-tag]
  (some? (node-hierarchy/tree type-tag)))

(def abstract?
  (memoize
   (fn [type-tag]
     (when-not (declared? type-tag)
       (throw (node-exceptions/construct node-exceptions/abstract node-exceptions/type-undeclared
                                         (str "Type with tag \"" type-tag "\" is undeclared"))))
     (when-not (defined? type-tag)
       (throw (node-exceptions/construct node-exceptions/abstract node-exceptions/type-undefined
                                         (str "Type with tag \"" type-tag "\" is undefined"))))
     (utils/in-list? abstract-types-tags-list type-tag))))

;; +----------------------------------------+
;; |                                        |
;; |   NODE TYPE RELATED PROPERTY GETTERS   |
;; |                                        |
;; +----------------------------------------+

(defn- get-property
  [type-tag property]
  (when-not (declared? type-tag)
    (throw (node-exceptions/construct node-exceptions/get-property node-exceptions/type-undeclared
                                      (str "Type with tag \"" type-tag "\" is undeclared"))))
  (when-not (defined? type-tag)
    (throw (node-exceptions/construct node-exceptions/get-property node-exceptions/type-undefined
                                      (str "Type with tag \"" type-tag "\" is undefined"))))
  ((node-hierarchy/tree type-tag) property))

(def get-label          (memoize (fn [type-tag] (get-property type-tag node-properties/label))))
(def get-super-type-tag (memoize (fn [type-tag] (get-property type-tag node-properties/super-type-tag))))
(def get-inputs         (memoize (fn [type-tag] (get-property type-tag node-properties/inputs))))
(def get-outputs        (memoize (fn [type-tag] (get-property type-tag node-properties/outputs))))
(def get-links          (memoize (fn [type-tag] (get-property type-tag node-properties/links))))
(def get-fields-tags    (memoize (fn [type-tag] (->> (get-property type-tag node-properties/fields-tags)
                                                     (remove (set base-node-fields/tags-list))))))

;; +-------------------------------+
;; |                               |
;; |   NODE TYPE RELATED GETTERS   |
;; |                               |
;; +-------------------------------+

(def get-inputs-number
  (memoize
   (fn [type-tag]
     (count (get-inputs type-tag)))))

(def get-outputs-number
  (memoize
   (fn [type-tag]
     (count (get-outputs type-tag)))))

(def get-inputs-tags
  (memoize
   (fn [type-tag]
     (->> (get-inputs type-tag)
          (map node-input-methods/get-tag)
          vec))))

(def get-outputs-tags
  (memoize
   (fn [type-tag]
       (->> (get-outputs type-tag)
            (map node-output-methods/get-tag)
            vec))))

(def get-linked-inputs-tags
  (memoize
   (fn [type-tag]
     (->> (get-links type-tag)
          (map node-link-methods/get-inputs-tags)
          flatten
          vec))))

(def get-linked-outputs-tags
  (memoize
   (fn [type-tag]
     (->> (get-links type-tag)
          (map node-link-methods/get-outputs-tags)
          flatten
          vec))))

(def get-input-channel-type
  (memoize
   (fn [type-tag input-tag]
     ((get-property type-tag node-properties/inputs-map) input-tag))))

(def get-output-channel-type
  (memoize
   (fn [type-tag output-tag]
     ((get-property type-tag node-properties/outputs-map) output-tag))))

;; +------------------------------------------+
;; |                                          |
;; |   NODE TYPE DEFINING RELATED FUNCTIONS   |
;; |                                          |
;; +------------------------------------------+

(defn- combine-inputs
  [label super-type-label type-inputs super-type-inputs]
  (if (some? type-inputs)
    (do
      (let [type-inputs-tags           (map node-input-methods/get-tag type-inputs)
            super-type-inputs-tags     (map node-input-methods/get-tag super-type-inputs)
            type-inputs-tags-set       (set type-inputs-tags)
            super-type-inputs-tags-set (set super-type-inputs-tags)]
        (when-not (empty? (cljset/intersection type-inputs-tags-set super-type-inputs-tags-set))
          (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-inputs-tags-intersection
                                            (str "Inputs tags of type labeled \"" label "\" intersect with inputs tags of super type labeled \"" super-type-label "\"")))))
      (into super-type-inputs type-inputs))
    super-type-inputs))

(defn- combine-outputs
  [label super-type-label type-outputs super-type-outputs]
  (if (some? type-outputs)
    (do
      (let [type-outputs-tags           (map node-output-methods/get-tag type-outputs)
            super-type-outputs-tags     (map node-output-methods/get-tag super-type-outputs)
            type-outputs-tags-set       (set type-outputs-tags)
            super-type-outputs-tags-set (set super-type-outputs-tags)]
        (when-not (empty? (cljset/intersection type-outputs-tags-set super-type-outputs-tags-set))
          (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-outputs-tags-intersection
                                            (str "Outputs tags of type labeled \"" label "\" intersect with outputs tags of super type labeled \"" super-type-label "\"")))))
      (into super-type-outputs type-outputs))
    super-type-outputs))

(defn- combine-links
  [label super-type-label type-links super-type-links]
  (if (some? type-links)
    (do
      (let [type-links-flatten-inputs-tags           (flatten (map node-link-methods/get-inputs-tags type-links))
            super-type-links-flatten-inputs-tags     (flatten (map node-link-methods/get-inputs-tags super-type-links))
            type-links-flatten-inputs-tags-set       (set type-links-flatten-inputs-tags)
            super-type-links-flatten-inputs-tags-set (set super-type-links-flatten-inputs-tags)]
        (when-not (empty? (cljset/intersection type-links-flatten-inputs-tags-set super-type-links-flatten-inputs-tags-set))
          (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-links-inputs-tags-intersection
                                            (str "Inputs tags of links of type labeled \"" label "\" intersect with inputs tags of links of super type labeled \"" super-type-label "\"")))))
      (into super-type-links type-links))
    super-type-links))

(defn- initialize-inputs-map
  [inputs]
  (reduce #(assoc %1 (node-input-methods/get-tag %2) (node-input-methods/get-channel-type %2)) {} inputs))

(defn- initialize-outputs-map
  [outputs]
  (reduce #(assoc %1 (node-output-methods/get-tag %2) (node-output-methods/get-channel-type %2)) {} outputs))

(defn- initialize-links-map
  [links]
  (reduce #(assoc %1 (node-link-methods/get-inputs-tags %2) [(node-link-methods/get-handler %2) (node-link-methods/get-outputs-tags %2)]) {} links))

(defn- combine-fields-tags
  [label super-type-label type-fields-tags super-type-fields-tags]
  (when-not (empty? (cljset/intersection (set type-fields-tags) (set super-type-fields-tags)))
    (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-fields-tags-intersection
                                      (str "Fields of type labeled \"" label "\" intersect with fields of super type labeled \"" super-type-label "\""))))
  (into super-type-fields-tags type-fields-tags))

(defn- create
  [label super-type-tag properties-map]
  (let [type-inputs (properties-map node-properties/inputs)]
    (when (some? type-inputs)
      (when-not (vector? type-inputs)
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/inputs-not-vector
                                          (str "Inputs of type labeled \"" label "\" are not given as a vector"))))
      (when-not (= (count type-inputs) (count (set (map node-input-methods/get-tag type-inputs))))
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/duplicated-inputs-tags
                                          (str "Inputs tags of type labeled \"" label "\" are duplicated"))))))
  (let [type-outputs (properties-map node-properties/outputs)]
    (when (some? type-outputs)
      (when-not (vector? type-outputs)
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/outputs-not-vector
                                          (str "Outputs of type labeled \"" label "\" are not given as a vector"))))
      (when-not (= (count type-outputs) (count (set (map node-output-methods/get-tag type-outputs))))
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/duplicated-outputs-tags
                                          (str "Outputs tags of type labeled \"" label "\" are duplicated"))))))
  (let [type-links (properties-map node-properties/links)]
    (when (some? type-links)
      (when-not (vector? type-links)
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/links-not-vector
                                          (str "Links of type labeled \"" label "\" are not given as a vector"))))
      (let [type-links-flatten-inputs-tags (flatten (map node-link-methods/get-inputs-tags type-links))]
        (when-not (= (count type-links-flatten-inputs-tags) (count (set type-links-flatten-inputs-tags)))
          (throw (node-exceptions/construct node-exceptions/define node-exceptions/duplicated-links-inputs-tags
                                            (str "Inputs tags of links of type labeled \"" label "\" are duplicated")))))))
  (let [type-fields-tags (properties-map node-properties/fields-tags)]
    (when (some? type-fields-tags)
      (when-not (vector? type-fields-tags)
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/fields-tags-not-vector
                                          (str "Fields tags of type labeled \"" label "\" are not given as a vector"))))
      (when-not (= (count type-fields-tags) (count (set type-fields-tags)))
        (throw (node-exceptions/construct node-exceptions/define node-exceptions/duplicated-fields-tags
                                          (str "Fields tags of type labeled \"" label "\" are duplicated"))))))
  (let [node-label           label
        node-super-type-tag  super-type-tag
        node-inputs          (combine-inputs node-label (get-label super-type-tag)
                                             (properties-map node-properties/inputs)
                                             (get-inputs node-super-type-tag))
        node-outputs         (combine-outputs node-label (get-label super-type-tag)
                                              (properties-map node-properties/outputs)
                                              (get-outputs node-super-type-tag))
        node-links           (combine-links node-label (get-label super-type-tag)
                                            (properties-map node-properties/links)
                                            (get-links node-super-type-tag))
        node-inputs-map      (initialize-inputs-map  node-inputs)
        node-outputs-map     (initialize-outputs-map node-outputs)
        node-links-map       (initialize-links-map   node-links)
        node-fields-tags     (combine-fields-tags node-label (get-label super-type-tag)
                                                  (properties-map node-properties/fields-tags)
                                                  (get-property super-type-tag node-properties/fields-tags))]
    (hash-map node-properties/label          node-label
              node-properties/super-type-tag node-super-type-tag
              node-properties/inputs         node-inputs
              node-properties/outputs        node-outputs
              node-properties/links          node-links
              node-properties/inputs-map     node-inputs-map
              node-properties/outputs-map    node-outputs-map
              node-properties/links-map      node-links-map
              node-properties/fields-tags    node-fields-tags)))

(defn- internal-define
  [label type-tag properties-map]
  (let [super-type-tag (or (properties-map node-properties/super-type-tag) NodeT)]
    (when-not (declared? type-tag)
      (throw (node-exceptions/construct node-exceptions/define node-exceptions/type-undeclared
                                        (str "Type with tag \"" type-tag "\" is undeclared"))))
    (when (defined? type-tag)
      (throw (node-exceptions/construct node-exceptions/define node-exceptions/type-defined
                                        (str "Type with tag \"" type-tag "\" is already defined"))))
    (when-not (declared? super-type-tag)
      (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-undeclared
                                        (str "Type (given as super) with tag \"" super-type-tag "\" is undeclared "))))
    (when-not (defined? super-type-tag)
      (throw (node-exceptions/construct node-exceptions/define node-exceptions/super-type-undefined
                                        (str "Type (given as super) with tag \"" super-type-tag "\" is undefined"))))
    (dosync
     (alter node-hierarchy/tree #(assoc % type-tag (create label super-type-tag properties-map))))))

(defn define
  [label type-tag & properties]
  (internal-define label type-tag (apply hash-map properties)))
