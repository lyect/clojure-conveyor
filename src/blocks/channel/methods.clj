(ns blocks.channel.methods
  (:require [clojure.set               :as cljset]
            [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]
            [blocks.channel.types      :as channel-types]
            [utils]))


;; +--------------------------------+
;; |                                |
;; |   CHANNEL RELATED PREDICATES   |
;; |                                |
;; +--------------------------------+

(defn channel?
  "Predicate to check whether _obj_ is channel or not"
  [obj]
  (and (some? obj)
       (map? obj)
       (contains? obj channel-properties/type-name)
       (channel-types/defined? (obj channel-properties/type-name))
       (utils/lists-equal? (keys (dissoc obj channel-properties/type-name))
                           ((channel-hierarchy/tree (obj channel-properties/type-name)) channel-properties/fields))))

;; +-------------------------------------------+
;; |                                           |
;; |   CHANNEL TYPE RELATED PROPERTY GETTERS   |
;; |                                           |
;; +-------------------------------------------+

(defn- get-channel-property
  "Get _property_ of _channel_"
  [channel property]
  {:pre [(channel? channel)]}
  ((channel-hierarchy/tree (channel channel-properties/type-name)) property))

;; No need to check whether "channel" is a correct channel or not
;; It will be done inside "get-channel-property"
(defn get-channel-type-name  [channel] (get-channel-property channel channel-properties/type-name))
(defn get-channel-super-name [channel] (get-channel-property channel channel-properties/super-name))
(defn get-channel-fields     [channel] (get-channel-property channel channel-properties/fields))

;; +------------------------------------+
;; |                                    |
;; |   CHANNEL INSTANCE FIELDS GETTER   |
;; |                                    |
;; +------------------------------------+

(defn get-channel-field
  "Get field's value of _channel_ by _field-keyword_"
  [channel field-keyword]
  {:pre [(channel? channel) (channel field-keyword)]}
  (channel field-keyword))

;; +-------------------------+
;; |                         |
;; |   CHANNEL CONSTRUCTOR   |
;; |                         |
;; +-------------------------+

(defn create
  "Create channel typed as _channel-type-keyword_, fills fields with _fields_.
   _fields_ must be a collection consisting of pairs such as (f1 v1 f2 v2 ... fn vn)"
  [channel-type-keyword & fields]
  {:pre [(channel-types/defined? channel-type-keyword)]}
  (let [fields-map              (apply hash-map fields)
        channel-fields          (keys fields-map)
        channel-type-fields     ((channel-hierarchy/tree channel-type-keyword) channel-properties/fields)
        channel-fields-set      (set channel-fields)
        channel-type-fields-set (set channel-type-fields)
        channel                 (ref {})]
    (when-not (<= (count (take-nth 2 fields)) (count channel-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/duplicating-fields
                                        (str "Tried to create " channel-type-keyword " with duplicated fields"))))
    (when-not (empty? (cljset/difference channel-type-fields-set channel-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/missing-fields
                                           (str "Tried to create " channel-type-keyword " with missing fields"))))
    (when-not (empty? (cljset/difference channel-fields-set channel-type-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/excess-fields
                                           (str "Tried to create " channel-type-keyword " with excess fields"))))
    (dosync
      (doseq [fields-map-entry fields-map]
        (alter channel #(assoc % (first fields-map-entry) (second fields-map-entry))))
      (alter channel #(assoc % channel-properties/type-name channel-type-keyword)))))
