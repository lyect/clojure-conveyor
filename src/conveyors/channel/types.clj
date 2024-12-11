(ns conveyors.channel.types
  (:require [conveyors.channel.hierarchy  :as channel-hierarchy]
            [conveyors.channel.properties :as channel-properties]))

(def Channel ::channeltype-Channel)

; Derived classes
; (def NewChannel ::channeltype-NewChannel)

(def types-list [])


(defn get-type-super [ch-type]
  ((@channel-hierarchy/tree ch-type) channel-properties/super))

(defn subtype? [ch-type1 ch-type2] "true if ch-type1 is subtype of ch-type2"
  (cond (= ch-type1 ch-type2) true
        (= ch-type1 Channel) false
        :default (subtype? (get-type-super ch-type1) ch-type2)))
