(ns blocks.channel.base
  (:require [clojure.set               :as cljset]
            [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]
            [blocks.channel.types      :as channel-types]
            [utils]))


; Alters hierarchy tree with Channel (base type for all the channel types) only once
(dosync (when-not (some? (channel-hierarchy/tree channel-types/Channel))
         (alter channel-hierarchy/tree #(assoc % channel-types/Channel {channel-properties/type-name  channel-types/Channel
                                                                        channel-properties/super-name nil
                                                                        channel-properties/fields     ()}))))

(defn append-channel-hierarchy
  "Alter tree hierarchy with _new-channel-type_ if not defined and super is defined"
  [new-channel-type]
  {:pre [(contains? @channel-hierarchy/tree (new-channel-type channel-properties/super-name))]}
  (let [new-channel-type-name (new-channel-type channel-properties/type-name)]
    (when (channel-hierarchy/tree new-channel-type-name)
      (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/type-defined
                                           (str "Type with name \"" new-channel-type-name "\" is already defined"))))
    (dosync (alter channel-hierarchy/tree #(assoc % new-channel-type-name new-channel-type)))))

(defmacro define-channel-type
  "Define a channel with _name_ and _properties_"
  [name & properties]
  `(let [properties-map#      (hash-map ~@properties)
         super-name#          (or (properties-map# channel-properties/super-name) channel-types/Channel)
         super#               (channel-hierarchy/tree super-name#)
         new-type-fields#     (properties-map# channel-properties/fields)
         super-fields#        (super# channel-properties/fields)
         new-type-fields-set# (set new-type-fields#)
         super-fields-set#    (set super-fields#)]
     (when-not (utils/in-list? channel-types/types-list ~name)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/type-not-declared
                                         (str "Type with name \"" ~name "\" is not declared"))))
     (when-not (<= (count new-type-fields#) (count new-type-fields-set#))
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/duplicating-fields
                                            (str "Fields of type named \"" ~name "\" are duplicated"))))
     (when-not (empty? (cljset/intersection new-type-fields-set# super-fields-set#))
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/super-fields-intersection
                                            (str "Fields of type named \"" ~name "\" intersect with fields of " (super-# channel-properties/type-name)))))
     (append-channel-hierarchy {channel-properties/type-name  ~name
                                channel-properties/super-name super-name#
                                channel-properties/fields     (concat new-type-fields# super-fields#)})
     (when-not (utils/lists-equal? channel-properties/properties-list (keys (channel-hierarchy/tree ~name)))
       (do
         (dosync (alter channel-hierarchy/tree #(dissoc % ~name)))
         (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/channel-properties-missing
                                              "Not all channel-properties are added to the \"define-channel-type\" macro"))))))
