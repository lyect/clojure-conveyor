(ns blocks.node.types
  (:require [blocks.node.hierarchy  :as node-hierarchy]
            [blocks.node.properties :as node-properties]))


;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def NodeT ::nodetype-Node)

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
(def RGBSplitT     ::nodetype-RGBSplit)
(def SharpenT      ::nodetype-Sharpen)

(def types-list [NodeT ConcatT CutT DenoiseT DifferenceT GammaT Image2BitmapT RGBSplitT SharpenT])

;; +-------------------------------------+
;; |                                     |
;; |   CHANNEL TYPE RELATED PREDICATES   |
;; |                                     |
;; +-------------------------------------+

(defn defined?
  "Check whether node type named as _type-keyword_ is defined or not"
  [type-keyword]
  (some? (node-hierarchy/tree type-keyword)))

(defn subtype?
  "Check whether node type named _node-type-name1_ is subtype of node type named _node-type-name2_ or not"
  [node-type-name1 node-type-name2] 
  (cond (= node-type-name1 node-type-name2) true
        (= node-type-name1 NodeT) false
        :else (subtype? ((node-hierarchy/tree node-type-name1) node-properties/super-name)
                        node-type-name2)))
