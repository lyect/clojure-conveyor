(ns blocks.channel.types
  (:require [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.exceptions                 :as channel-exceptions]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [clojure.set                               :as cljset]
            [utils]
            ))


;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def ChannelT ::channeltype-ChannelT)

;; +-----------------------------------+
;; |                                   |
;; |   KEYWORDS FOR ABSTRACT CLASSES   |
;; |                                   |
;; +-----------------------------------+

(def ImageT  ::channeltype-ImageT)
(def NumberT ::channeltype-NumberT)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def JpegT    ::channeltype-Jpeg)
(def PngT     ::channeltype-Png)
(def BitmapT  ::channeltype-Bitmap)
(def FloatT   ::channeltype-Float)
(def IntegerT ::channeltype-Integer)

(def ^:private types-tags-list [; Base type
                                ChannelT
                                ; Abstract types
                                ImageT
                                NumberT
                                ; Derived types
                                JpegT
                                PngT
                                BitmapT
                                FloatT
                                IntegerT])

(def ^:private abstract-types-tags-list [ImageT
                                         NumberT])

;; +-------------------------------------+
;; |                                     |
;; |   CHANNEL TYPE RELATED PREDICATES   |
;; |                                     |
;; +-------------------------------------+

(defn declared?
  [type-tag]
  (utils/in-list? types-tags-list type-tag))

(defn defined?
  [type-tag]
  (some? (channel-hierarchy/tree type-tag)))

(defn abstract?
  [type-tag]
  (when-not (declared? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/abstract channel-exceptions/type-undeclared
                                         (str "Type with tag \"" type-tag "\" is undeclared"))))
  (when-not (declared? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/abstract channel-exceptions/type-undefined
                                         (str "Type with tag \"" type-tag "\" is undefined"))))
  (utils/in-list? abstract-types-tags-list type-tag))

(defn subtype?
  [derived-type-tag type-tag] 
  (cond (= derived-type-tag type-tag) true
        (= derived-type-tag ChannelT) false
        :else (subtype? ((channel-hierarchy/tree derived-type-tag) channel-properties/super-type-tag)
                        type-tag)))

;; +-------------------------------------------+
;; |                                           |
;; |   CHANNEL TYPE RELATED PROPERTY GETTERS   |
;; |                                           |
;; +-------------------------------------------+

(defn- get-property
  [type-tag property]
  (when-not (declared? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/get-property channel-exceptions/type-undeclared
                                         (str "Type with tag \"" type-tag "\" is undeclared"))))
  (when-not (defined? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/get-property channel-exceptions/type-undefined
                                         (str "Type with tag \"" type-tag "\" is undefined"))))
  ((channel-hierarchy/tree type-tag) property))

(def get-label          (memoize (fn [type-tag] (get-property type-tag channel-properties/label))))
(def get-super-type-tag (memoize (fn [type-tag] (get-property type-tag channel-properties/super-type-tag))))
(def get-fields-tags    (memoize (fn [type-tag] (->> (get-property type-tag channel-properties/fields-tags)
                                                     (remove (set base-channel-fields/tags-list))))))

;; +---------------------------------------------+
;; |                                             |
;; |   CHANNEL TYPE DEFINING RELATED FUNCTIONS   |
;; |                                             |
;; +---------------------------------------------+

(defn- combine-fields-tags
  [label super-type-label type-fields-tags super-type-fields-tags]
  (when-not (empty? (cljset/intersection (set type-fields-tags) (set super-type-fields-tags)))
    (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/super-type-fields-tags-intersection
                                      (str "Fields of type labeled \"" label "\" intersect with fields of super type labeled \"" super-type-label "\""))))
  (into super-type-fields-tags type-fields-tags))

(defn- create
  [label super-type-tag properties-map]
  (let [type-fields-tags (properties-map channel-properties/fields-tags)]
    (when (some? type-fields-tags)
      (when-not (vector? type-fields-tags)
        (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/fields-tags-not-vector
                                          (str "Fields tags of type labeled \"" label "\" are not given as a vector"))))
      (when-not (= (count type-fields-tags) (count (set type-fields-tags)))
        (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/duplicated-fields-tags
                                          (str "Fields tags of type labeled \"" label "\" are duplicated"))))))
  (let [channel-label          label
        channel-super-type-tag super-type-tag
        channel-fields-tags    (combine-fields-tags label (get-label super-type-tag)
                                                    (properties-map channel-properties/fields-tags)
                                                    (get-property channel-super-type-tag channel-properties/fields-tags))]
    (hash-map channel-properties/label          channel-label
              channel-properties/super-type-tag channel-super-type-tag
              channel-properties/fields-tags    channel-fields-tags)))

(defn- internal-define
  [label type-tag properties-map]
  (let [super-type-tag (or (properties-map channel-properties/super-type-tag) ChannelT)]
    (when-not (declared? type-tag)
      (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/type-undeclared
                                           (str "Type with tag \"" type-tag "\" is undeclared"))))
    (when (defined? type-tag)
      (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/type-defined
                                           (str "Type with tag \"" type-tag "\" is already defined"))))
    (when-not (declared? super-type-tag)
      (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/super-type-undeclared
                                           (str "Type (given as super) with tag \"" super-type-tag "\" is undeclared "))))
    (when-not (defined? super-type-tag)
      (throw (channel-exceptions/construct channel-exceptions/define channel-exceptions/super-type-undefined
                                           (str "Type (given as super) with tag \"" super-type-tag "\" is undefined"))))
    (dosync
     (alter channel-hierarchy/tree #(assoc % type-tag (create label super-type-tag properties-map))))))

(defn define
  [label type-tag & properties]
  (internal-define label type-tag (apply hash-map properties)))