(ns blocks.node.types
  (:require [blocks.node.hierarchy :as node-hierarchy]
            [utils]))


;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def NodeT ::nodetype-Node)

;; +-----------------------------------+
;; |                                   |
;; |   KEYWORDS FOR ABSTRACT CLASSES   |
;; |                                   |
;; +-----------------------------------+

(def SelectorT  ::nodetype-Selector)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def ConcatT       ::nodetype-Concat)
(def CutT          ::nodetype-Cut)
(def DenoiseT      ::nodetype-Denoise)
(def DifferenceT   ::nodetype-Difference)
(def GammaT        ::nodetype-Gamma)
(def Image2BitmapT ::nodetype-Image2Bitmap)
(def Image2ImageT  ::nodetype-Image2Image)
(def RGBSplitT     ::nodetype-RGBSplit)
(def SharpenT      ::nodetype-Sharpen)

(def ^:private types-list [; Base type
                           NodeT
                           ; Abstract types
                           SelectorT
                           ; Derived types
                           ConcatT
                           CutT
                           DenoiseT
                           DifferenceT
                           GammaT
                           Image2BitmapT
                           Image2ImageT
                           RGBSplitT
                           SharpenT])

(def ^:private abstract-types-list [NodeT
                                    SelectorT])

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
  (utils/in-list? abstract-types-list type-keyword))

(defn defined?
  [type-keyword]
  (some? (node-hierarchy/tree type-keyword)))

;; +---------------------------------------+
;; |                                       |
;; |   CHANNEL TYPE LIST RELATED METHODS   |
;; |                                       |
;; +---------------------------------------+

(defn clear-type-list
  []
  (alter-var-root
   types-list
   (fn [_] [])))

(defn clear-abstract-type-list
  []
  (alter-var-root
   abstract-types-list
   (fn [_] [])))

(defn add-type-list
  [type-keyword]
  (alter-var-root
   types-list
   (fn [_]
     (if (utils/in-list? types-list type-keyword)
       types-list
       (into types-list [type-keyword])))))

(defn add-abstract-type-list
  [type-keyword]
  (alter-var-root
   abstract-types-list
   (fn [_]
     (if (utils/in-list? abstract-types-list type-keyword)
       abstract-types-list
       (into abstract-types-list [type-keyword])))))
