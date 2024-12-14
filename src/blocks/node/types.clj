(ns blocks.node.types
  (:require [blocks.node.hierarchy  :as node-hierarchy]
            [blocks.node.properties :as node-properties]))


;; +----------------------------+
;; |                            |
;; |   KEYWORD FOR BASE CLASS   |
;; |                            |
;; +----------------------------+

(def Node ::nodetype-Node)

;; +-----------------------------------------------+
;; |                                               |
;; |   KEYWORDS RELATED TO ALL THE DERIVED TYPES   |
;; |                                               |
;; +-----------------------------------------------+

(def Jpeg2BitmapT ::nodetype-Jpeg2Bitmap)
(def Png2BitmapT  ::nodetype-Png2Bitmap)
(def CropT        ::nodetype-Crop)
(def MonochromeT  ::nodetype-Monochrome)
(def InversionT   ::nodetype-Inversion)
(def GammaT       ::nodetype-Gamma)
(def SharpenT     ::nodetype-Sharpen)
(def GaussT       ::nodetype-Gauss)
(def DifferenceT  ::nodetype-difference)

(def types-list [Node Jpeg2BitmapT Png2BitmapT CropT MonochromeT InversionT GammaT SharpenT GaussT DifferenceT])

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
        (= node-type-name1 Node) false
        :else (subtype? ((node-hierarchy/tree node-type-name1) node-properties/super-name)
                        node-type-name2)))
