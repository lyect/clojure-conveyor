(ns blocks.channel.types
  (:require [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]))

(def Channel ::channeltype-Channel)


(def Bitmap ::channeltype-Bitmap)
(def JPEG   ::channeltype-JPEG)
(def PNG    ::channeltype-PNG)
(def Kernel ::channeltype-Kernel)

; Derived classes
; (def NewChannel ::channeltype-NewChannel)

(def types-list [Bitmap JPEG PNG Kernel])


(defn get-type-super [ch-type]
  ((channel-hierarchy/tree ch-type) channel-properties/super))

(defn subtype? [ch-type1 ch-type2] "true if ch-type1 is subtype of ch-type2"
  (cond (= ch-type1 ch-type2) true
        (= ch-type1 Channel) false
        :else (subtype? (get-type-super ch-type1) ch-type2)))