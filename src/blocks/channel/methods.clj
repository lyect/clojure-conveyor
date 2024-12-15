(ns blocks.channel.methods
  (:require [clojure.set               :as cljset]
            [blocks.channel.base       :as channel-base]
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
       (contains? obj channel-base/type-name)
       (channel-types/defined? (obj channel-base/type-name))
       (utils/lists-equal? (keys (dissoc obj channel-base/type-name))
                           ((channel-hierarchy/tree (obj channel-base/type-name)) channel-properties/fields))))

;; +-------------------------------------------+
;; |                                           |
;; |   CHANNEL TYPE RELATED PROPERTY GETTERS   |
;; |                                           |
;; +-------------------------------------------+

;; No need to check whether channel has the property or not. get-channel-property is a private function,
;; hence it is used only for specific properties
(defn- get-channel-property
  "Get _property_ of _channel_"
  [channel property]
  (when-not (channel? channel)
    (throw (channel-exceptions/construct channel-exceptions/get-channel-property channel-exceptions/not-channel
                                         (str "\"" channel "\" is not a channel"))))
  ((channel-hierarchy/tree (channel channel-base/type-name)) property))

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
  "Get field's value of _channel_ by _field-name_"
  [channel field-name]
  (when-not (channel? channel)
    (throw (channel-exceptions/construct channel-exceptions/get-channel-field channel-exceptions/not-channel
                                         (str "\"" channel "\" is not a channel"))))
  (when-not (channel field-name)
    (throw (channel-exceptions/construct channel-exceptions/get-channel-field channel-exceptions/unknown-field
                                         (str "\"" channel "\" has no field named \"" field-name "\""))))
  (channel field-name))

;; +-------------------------+
;; |                         |
;; |   CHANNEL CONSTRUCTOR   |
;; |                         |
;; +-------------------------+

(defn create
  "Create channel typed as _channel-type-name_, fills fields with _fields_.
   _fields_ must be a collection consisting of pairs such as (f1 v1 f2 v2 ... fn vn)"
  [channel-type-name & fields]
  (when-not (utils/in-list? channel-types/types-list channel-type-name)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/type-undeclared
                                         (str "Type named \"" channel-type-name "\" is undeclared"))))
  (when-not (channel-types/defined? channel-type-name)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/type-undefined
                                          (str "Type named \"" channel-type-name "\" is undefined"))))
  (when (= channel-type-name channel-types/Channel)
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/abstract-creation
                                         (str "Unable to instantiate abstract channel"))))
  (let [fields-map              (apply hash-map fields)
        channel-fields          (keys fields-map)
        channel-type-fields     ((channel-hierarchy/tree channel-type-name) channel-properties/fields)
        channel-fields-set      (set channel-fields)
        channel-type-fields-set (set channel-type-fields)
        channel                 (ref {})]
    (when (> (count (take-nth 2 fields)) (count channel-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/duplicating-fields
                                           (str "Tried to create " channel-type-name " with duplicated fields"))))
    (when-not (empty? (cljset/difference channel-type-fields-set channel-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/missing-fields
                                           (str "Tried to create " channel-type-name " with missing fields"))))
    (when-not (empty? (cljset/difference channel-fields-set channel-type-fields-set))
      (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/excess-fields
                                           (str "Tried to create " channel-type-name " with excess fields"))))
    (doseq [fields-map-entry fields-map]
      (alter channel #(assoc % (first fields-map-entry) (second fields-map-entry))))
    (alter channel #(assoc % channel-base/type-name channel-type-name))))
