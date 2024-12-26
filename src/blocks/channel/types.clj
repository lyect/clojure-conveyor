(ns blocks.channel.types
  (:require [blocks.channel.exceptions :as channel-exceptions]
            [blocks.channel.hierarchy  :as channel-hierarchy]
            [blocks.channel.properties :as channel-properties]
            [utils]))


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

(def ^:private types-list [; Base type
                           ChannelT
                           ; Abstract types
                           ImageT
                           NumberT
                           ; Derived types
                           JpegT
                           PngT
                           BitmapT
                           FloatT
                           IntegerT
                           MatrixT])

(def ^:private abstract-types-list [ImageT
                                    NumberT])

;; +-------------------------------------+
;; |                                     |
;; |   CHANNEL TYPE RELATED PREDICATES   |
;; |                                     |
;; +-------------------------------------+

(defn declared?
  [type-keyword]
  (utils/in-list? types-list type-keyword))

(defn abstract?
  [type-keyword]
  (when-not (declared? type-keyword)
    (throw (channel-exceptions/construct channel-exceptions/abstract channel-exceptions/type-undeclared
                                         (str "Unable to check whether \"" type-keyword "\" is abstract or not since undeclared"))))
  (utils/in-list? abstract-types-list type-keyword))

(defn defined?
  [type-keyword]
  (some? (channel-hierarchy/tree type-keyword)))

(defn subtype?
  [channel-type-name1 channel-type-name2] 
  (cond (= channel-type-name1 channel-type-name2) true
        (= channel-type-name1 ChannelT) false
        :else (subtype? ((channel-hierarchy/tree channel-type-name1) channel-properties/super-name)
                        channel-type-name2)))
