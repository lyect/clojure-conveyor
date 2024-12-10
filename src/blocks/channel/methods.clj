(ns blocks.channel.methods
  (:require [clojure.set               :as cljset]
            [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]
            [blocks.channel.types      :as channel-types]))


(defn channel-type-defined?
  [type-keyword]
  (contains? @channel-hierarchy/tree type-keyword))


(defn- get-channel-property
  [channel property]
  {:pre [(let [channel-type (channel channel-properties/T)]
           (and
            (some? channel-type)
            (channel-type-defined? channel-type)))]}
  ((channel-hierarchy/tree (channel channel-properties/T)) property))


(defn get-channel-type    [channel] (get-channel-property channel channel-properties/T))
(defn get-channel-super   [channel] (get-channel-property channel channel-properties/super))
(defn get-channel-fields  [channel] (get-channel-property channel channel-properties/fields))


; Channel's fields
(defn get-channel-field
  [channel field-keyword]
  {:pre [(let [channel-type (channel channel-properties/T)]
           (and
            (some? channel-type)
            (channel-type-defined? channel-type)))]}
  (channel field-keyword))

; Constructor
(defn create
  [channel-type-keyword & fields]
  {:pre [(channel-type-defined? channel-type-keyword)]}
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
      (alter channel #(assoc % channel-properties/T channel-type-keyword)))))


(defn have-subtype? [ch1 ch2] "true if type ch1 is subtype of type ch2"
  (channel-types/subtype? (get-channel-type ch1) (get-channel-type ch2)))