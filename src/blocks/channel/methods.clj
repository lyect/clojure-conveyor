(ns blocks.channel.methods
  (:require [clojure.set                               :as cljset]
            [blocks.channel.definitions.channel.fields :as base-channel-fields]
            [blocks.channel.exceptions                 :as channel-exceptions]
            [blocks.channel.hierarchy                  :as channel-hierarchy]
            [blocks.channel.properties                 :as channel-properties]
            [blocks.channel.types                      :as channel-types]
            [utils]))


;; +--------------------------------+
;; |                                |
;; |   CHANNEL RELATED PREDICATES   |
;; |                                |
;; +--------------------------------+

(def channel?
  "Predicate to check whether _obj_ is channel or not"
  (memoize
   (fn [obj-ref]
     (and (some? @obj-ref)
          (map? @obj-ref)
          (obj-ref base-channel-fields/type-name)
          (channel-types/defined? (obj-ref base-channel-fields/type-name))
          (utils/lists-equal? (remove #{base-channel-fields/type-name} (keys @obj-ref))
                              (remove #{base-channel-fields/type-name}
                                      ((channel-hierarchy/tree (obj-ref base-channel-fields/type-name)) channel-properties/fields)))))))

;; +-------------------------------------------+
;; |                                           |
;; |   CHANNEL TYPE RELATED PROPERTY GETTERS   |
;; |                                           |
;; +-------------------------------------------+

;; No need to check whether channel has the property or not. get-channel-property is a private function,
;; hence it is used only for specific properties
(defn- get-channel-property
  "Get _property_ of _channel_"
  [channel-ref property]
  (when-not (channel? channel-ref)
    (throw (channel-exceptions/construct channel-exceptions/get-channel-property channel-exceptions/not-channel
                                         (str "\"" channel-ref "\" is not a channel"))))
  ((channel-hierarchy/tree (channel-ref base-channel-fields/type-name)) property))

;; No need to check whether "channel" is a correct channel or not
;; It will be done inside "get-channel-property"
(def get-channel-type-name  (memoize (fn [channel-ref] (get-channel-property channel-ref channel-properties/type-name))))
(def get-channel-super-name (memoize (fn [channel-ref] (get-channel-property channel-ref channel-properties/super-name))))
(def get-channel-fields     (memoize (fn [channel-ref] (remove (set base-channel-fields/fields-list) (get-channel-property channel-ref channel-properties/fields)))))

;; +------------------------------------+
;; |                                    |
;; |   CHANNEL INSTANCE FIELDS GETTER   |
;; |                                    |
;; +------------------------------------+

(defn get-channel-field
  "Get field's value of _channel_ by _field-name_"
  [channel-ref field-name]
  (when-not (channel? channel-ref)
    (throw (channel-exceptions/construct channel-exceptions/get-channel-field channel-exceptions/not-channel
                                         (str "\"" channel-ref "\" is not a channel"))))
  ; ChannelT fields protection
  (let [protected-channel (apply dissoc @channel-ref base-channel-fields/fields-list)]
    (when-not (protected-channel field-name)
      (throw (channel-exceptions/construct channel-exceptions/get-channel-field channel-exceptions/unknown-field
                                           (str "\"" channel-ref "\" has no field named \"" field-name "\""))))
    (protected-channel field-name)))

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
  (when (or (= channel-type-name channel-types/ChannelT)
            (= channel-type-name channel-types/ImageT)
            (= channel-type-name channel-types/NumberT))
    (throw (channel-exceptions/construct channel-exceptions/create channel-exceptions/abstract-creation
                                         (str "Unable to instantiate abstract channel"))))
  (let [fields-map              (apply hash-map fields)
        channel-fields          (keys fields-map)
        channel-type-fields     (remove (set base-channel-fields/fields-list) ((channel-hierarchy/tree channel-type-name) channel-properties/fields))
        channel-fields-set      (set channel-fields)
        channel-type-fields-set (set channel-type-fields)
        channel-ref             (ref {})]
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
      (alter channel-ref #(assoc % (first fields-map-entry) (second fields-map-entry))))
    (alter channel-ref #(assoc % base-channel-fields/type-name channel-type-name))
    channel-ref))
