(ns conveyors.channel.base
  (:require [clojure.set                  :as cljset]
            [conveyors.channel.exceptions :as channel-exceptions]
            [conveyors.channel.hierarchy  :as channel-hierarchy]
            [conveyors.channel.properties :as channel-properties]
            [conveyors.channel.types      :as channel-types]
            [conveyors.utils              :as utils]))


(dosync (alter channel-hierarchy/tree #(assoc % channel-types/Channel {channel-properties/T       channel-types/Channel
                                                                       channel-properties/super   nil
                                                                       channel-properties/fields  ()})))

(defn append-channel-hierarchy [new-channel-type]
  {:pre [(contains? @channel-hierarchy/tree (new-channel-type channel-properties/super))]}
  (dosync (alter channel-hierarchy/tree #(assoc % (new-channel-type channel-properties/T) new-channel-type))))


(defmacro define-channel-type [name & sections] "\"sections\" is a sequence of pairs"
  `(let [sec-map#          (hash-map ~@sections)
         super#            (or (sec-map# channel-properties/super) channel-types/Channel)
         super-desc#       (@channel-hierarchy/tree super#)
         sec-fields#       (sec-map# channel-properties/fields)
         super-fields#     (super-desc# channel-properties/fields)
         sec-fields-set#   (set sec-fields#)
         super-fields-set# (set super-fields#)]
     (when-not (utils/in-list? channel-types/types-list ~name)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/type-not-declared
                                         (str "Type " ~name " is not declared"))))
     (when-not (<= (count sec-fields#) (count sec-fields-set#))
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/duplicating-fields
                                            (str "Fields of " ~name " are duplicated"))))
     (when-not (empty? (cljset/intersection sec-fields-set# super-fields-set#))
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/super-fields-intersection
                                            (str "Fields of " ~name " intersect with fields of " (super-desc# channel-properties/T)))))
     (append-channel-hierarchy {channel-properties/T      ~name
                                channel-properties/super  super#
                                channel-properties/fields (concat sec-fields# super-fields#)})
     (when-not (utils/lists-equal? channel-properties/properties-list (keys (channel-hierarchy/tree ~name)))
       (do
         (dosync (alter channel-hierarchy/tree #(dissoc % ~name)))
         (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/channel-properties-missing
                                              "Not all channel-properties are added to define-channel-type macro"))))))
