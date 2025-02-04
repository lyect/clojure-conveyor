(ns blocks.edge.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::keyword-type-keyword)
(def cause-keyword ::keyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def create            ::type-create)
(def get-edge-property ::type-get-edge-property)

(def ^:private type-list [create
                          get-edge-property])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def not-edge                              ::cause-not-edge)
(def edge-properties-missing               ::cause-edge-properties-missing)

(def ^:private cause-list [not-edge
                           edge-properties-missing])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create            [edge-properties-missing]
                                            get-edge-property [not-edge]})

;; +---------------------------+
;; |                           |
;; |   EXCEPTION CONSTRUCTOR   |
;; |                           |
;; +---------------------------+

(defn construct
  "Construct exception with _type_, _cause_ and _message_"
  [type cause message]
  {:pre [(utils/in-list? type-list                          type)
         (utils/in-list? cause-list                         cause)
         (utils/in-list? (types-causes-correspondence type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))
