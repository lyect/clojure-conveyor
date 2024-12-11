(ns blocks.channel.types
  (:require [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]))

;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def Channel ::channeltype-Channel)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def Bitmap ::channeltype-Bitmap)
(def JPEG   ::channeltype-JPEG)
(def PNG    ::channeltype-PNG)
(def Kernel ::channeltype-Kernel)

(def types-list [Bitmap JPEG PNG Kernel])

;; +-------------------------------------+
;; |                                     |
;; |   CHANNEL TYPE RELATED PREDICATES   |
;; |                                     |
;; +-------------------------------------+

(defn defined?
  "Check whether type named as _type-keyword_ is defined or not"
  [type-keyword]
  (some? (channel-hierarchy/tree type-keyword)))

(defn subtype?
  "Check whether channel type named _channel-type-name1_ is subtype of channel type named _channel-type-name2_ or not"
  [channel-type-name1 channel-type-name2] 
  (cond (= channel-type-name1 channel-type-name2) true
        (= channel-type-name1 Channel) false
        :else (subtype? ((channel-hierarchy/tree channel-type-name1) channel-properties/super-name)
                        channel-type-name2)))
