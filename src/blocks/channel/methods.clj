(ns blocks.channel.methods
  (:require [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.exceptions                 :as channel-exceptions]
            [blocks.channel.types                      :as channel-types]
            [clojure.set                               :as cljset]
            [utils]))


;; +--------------------------------+
;; |                                |
;; |   CHANNEL RELATED PREDICATES   |
;; |                                |
;; +--------------------------------+

(def channel?
  (memoize
   (fn [obj-ref]
     (and (utils/ref? obj-ref)
          @obj-ref
          (map? @obj-ref)
          (reduce #(and %1 (some? (obj-ref %2))) true base-channel-fields/tags-list)
          (let [type-tag (obj-ref base-channel-fields/type-tag)]
            (and (channel-types/defined? type-tag)
                 (utils/lists-equal?  (remove (set base-channel-fields/tags-list) (keys @obj-ref))
                                      (channel-types/get-fields-tags type-tag))))))))

;; +------------------------------------+
;; |                                    |
;; |   CHANNEL INSTANCE FIELDS GETTER   |
;; |                                    |
;; +------------------------------------+

(defn get-field-value
  [channel-ref field-tag]
  (when-not (channel? channel-ref)
    (throw (channel-exceptions/construct channel-exceptions/get-field-value channel-exceptions/not-channel
                                         (str "\"" channel-ref "\" is not a channel"))))
  (when-not ((apply dissoc @channel-ref base-channel-fields/tags-list) field-tag)
    (throw (channel-exceptions/construct channel-exceptions/get-field-value channel-exceptions/unknown-field-tag
                                         (str "\"" channel-ref "\" has no field tagged \"" field-tag "\""))))
  (channel-ref field-tag))

(def get-type-tag
  (memoize
   (fn [channel-ref]
     (when-not (channel? channel-ref)
       (throw (channel-exceptions/construct channel-exceptions/get-type-tag channel-exceptions/not-channel
                                            (str "\"" channel-ref "\" is not a channel"))))
     (channel-ref base-channel-fields/type-tag))))

;; +-------------------------+
;; |                         |
;; |   CHANNEL CONSTRUCTOR   |
;; |                         |
;; +-------------------------+

(defn create
  [type-tag & fields]
  (when-not (channel-types/declared? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/type-undeclared
                                      (str "Type tagged \"" type-tag "\" is undeclared"))))
  (when-not (channel-types/defined? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/type-undefined
                                      (str "Type tagged \"" type-tag "\" is undefined"))))
  (when (channel-types/abstract? type-tag)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/type-abstract
                                      (str "Type tagged \"" type-tag "\" is abstract"))))
  (let [fields-map           (apply hash-map fields)
        fields-tags          (keys fields-map)
        fields-tags-set      (set fields-tags)
        type-fields-tags     (channel-types/get-fields-tags type-tag)
        type-fields-tags-set (set type-fields-tags)
        channel-ref          (ref {})]
    (when (> (count fields-tags) (count fields-tags-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/duplicated-fields-tags
                                        (str "Tried to create channel of type tagged \"" type-tag "\" with duplicated fields tags"))))
    (when-not (empty? (cljset/difference type-fields-tags-set fields-tags-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/missed-fields-tags
                                        (str "Tried to create channel of type tagged \"" type-tag "\" with missing fields tags"))))
    (when-not (empty? (cljset/difference fields-tags-set type-fields-tags-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/excess-fields-tags
                                        (str "Tried to create channel of type tagged \"" type-tag "\" with excess fields tags"))))
    (doseq [fields-map-entry fields-map]
      (alter channel-ref #(assoc % (first fields-map-entry) (second fields-map-entry))))
    (alter channel-ref (fn [channel] (assoc channel base-channel-fields/type-tag type-tag)))
    channel-ref))
