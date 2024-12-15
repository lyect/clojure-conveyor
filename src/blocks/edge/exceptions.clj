(ns blocks.edge.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::edgeexceptionkeyword-type-keyword)
(def cause-keyword ::edgeexceptionkeyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def create            ::edgeexceptiontype-create)
(def get-edge-property ::edgeexceptiontype-get-edge-property)

(def ^:private type-list [create
                          get-edge-property])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def begin-vertex-index-not-integer        ::edgeexceptioncause-begin-vertex-index-not-integer)
(def begin-vertex-output-index-not-integer ::edgeexceptioncause-begin-vertex-output-index-not-integer)
(def end-vertex-index-not-integer          ::edgeexceptioncause-end-vertex-index-not-integer)
(def end-vertex-input-index-not-integer    ::edgeexceptioncause-end-vertex-input-index-not-integer)
(def begin-vertex-index-negative           ::edgeexceptioncause-begin-vertex-index-negative)
(def begin-vertex-output-index-negative    ::edgeexceptioncause-begin-vertex-output-index-negative)
(def end-vertex-index-negative             ::edgeexceptioncause-end-vertex-index-negative)
(def end-vertex-input-index-negative       ::edgeexceptioncause-end-vertex-input-index-negative)
(def not-edge                              ::edgeexceptioncause-not-edge)
(def edge-properties-missing               ::edgeexceptioncause-edge-properties-missing)

(def ^:private cause-list [begin-vertex-index-not-integer
                           begin-vertex-output-index-not-integer
                           end-vertex-index-not-integer
                           end-vertex-input-index-not-integer
                           begin-vertex-index-negative
                           begin-vertex-output-index-negative
                           end-vertex-index-negative
                           end-vertex-input-index-negative
                           not-edge
                           edge-properties-missing])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create            [begin-vertex-index-not-integer
                                                               begin-vertex-output-index-not-integer
                                                               end-vertex-index-not-integer
                                                               end-vertex-input-index-not-integer
                                                               begin-vertex-index-negative
                                                               begin-vertex-output-index-negative
                                                               end-vertex-index-negative
                                                               end-vertex-input-index-negative
                                                               edge-properties-missing]
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
