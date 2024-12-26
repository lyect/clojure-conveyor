(ns blocks.channel.base
  (:require [clojure.set               :as cljset]
            [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]
            [blocks.channel.types      :as channel-types]
            [utils]))


;; +---------------------------------------------+
;; |                                             |
;; |   CHANNEL TYPE DEFINING RELATED FUNCTIONS   |
;; |                                             |
;; +---------------------------------------------+

(defn append-channel-hierarchy
  [new-channel-type]
  (let [new-channel-type-name (new-channel-type channel-properties/type-name)]
    (dosync (alter channel-hierarchy/tree #(assoc % new-channel-type-name new-channel-type)))))

(defmacro define-channel-type
  [new-channel-type-name & properties]
  `(let [properties-map#      (hash-map ~@properties)
         super-name#          (or (properties-map# channel-properties/super-name) channel-types/ChannelT)]
     (when-not (channel-types/declared? ~new-channel-type-name)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/type-undeclared
                                            (str "Type named \"" ~new-channel-type-name "\" is undeclared"))))
     (when (channel-hierarchy/tree ~new-channel-type-name)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/type-defined
                                            (str "Type named \"" ~new-channel-type-name "\" is already defined"))))
     (when-not (channel-types/declared? super-name#)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/super-undeclared
                                            (str "Super \"" super-name# "\" of \"" ~new-channel-type-name "\" is undeclared"))))
     (when-not (channel-types/defined? super-name#)
       (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/super-undefined
                                            (str "Super \"" super-name# "\" of \"" ~new-channel-type-name "\" is undefined"))))
     (let [super#               (channel-hierarchy/tree super-name#)
           new-type-fields#     (properties-map# channel-properties/fields)
           super-fields#        (super# channel-properties/fields)
           new-type-fields-set# (set new-type-fields#)
           super-fields-set#    (set super-fields#)]
        (when-not (<= (count new-type-fields#) (count new-type-fields-set#))
          (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/duplicating-fields
                                               (str "Fields of type named \"" ~new-channel-type-name "\" are duplicated"))))
        (when-not (empty? (cljset/intersection new-type-fields-set# super-fields-set#))
          (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/super-fields-intersection
                                               (str "Fields of type named \"" ~new-channel-type-name "\" intersect with fields of super type named \"" super-name# "\""))))
        (append-channel-hierarchy {channel-properties/type-name  ~new-channel-type-name
                                   channel-properties/super-name super-name#
                                   channel-properties/fields     (concat new-type-fields# super-fields#)})
        (when-not (utils/lists-equal? channel-properties/properties-list (keys (channel-hierarchy/tree ~new-channel-type-name)))
          (do
            (dosync (alter channel-hierarchy/tree #(dissoc % ~new-channel-type-name)))
            (throw (channel-exceptions/construct channel-exceptions/define-channel-type channel-exceptions/channel-properties-missing
                                                 "Not all channel-properties are added to the \"define-channel-type\" macro")))))))
