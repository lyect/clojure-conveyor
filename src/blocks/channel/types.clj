(ns blocks.channel.types
  (:require [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]))


;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def ChannelT ::channeltype-ChannelT)

;; +-----------------------------------+
;; |                                   |
;; |   KEYWORDS FOR ABSTRACT CLASSES   |
;; |                                   |
;; +-----------------------------------+

(def ImageT  ::channeltype-ImageT)
(def NumberT ::channeltype-NumberT)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def JpegT    ::channeltype-Jpeg)
(def PngT     ::channeltype-Png)
(def BitmapT  ::channeltype-Bitmap)
(def FloatT   ::channeltype-Float)
(def IntegerT ::channeltype-Integer)
(def MatrixT  ::channeltype-Matrix)

(def types-list [ChannelT ImageT NumberT JpegT PngT BitmapT FloatT IntegerT MatrixT])

;; +-------------------------------------+
;; |                                     |
;; |   CHANNEL TYPE RELATED PREDICATES   |
;; |                                     |
;; +-------------------------------------+

(defn defined?
  "Check whether channel type named as _type-keyword_ is defined or not"
  [type-keyword]
  (some? (channel-hierarchy/tree type-keyword)))

(defn subtype?
  "Check whether channel type named _channel-type-name1_ is subtype of channel type named _channel-type-name2_ or not"
  [channel-type-name1 channel-type-name2] 
  (cond (= channel-type-name1 channel-type-name2) true
        (= channel-type-name1 ChannelT) false
        :else (subtype? ((channel-hierarchy/tree channel-type-name1) channel-properties/super-name)
                        channel-type-name2)))
