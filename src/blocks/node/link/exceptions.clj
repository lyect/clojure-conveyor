(ns blocks.node.link.exceptions
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

(def create       ::type-create)
(def get-property ::type-get-property)

(def ^:private type-list [create
                          get-property])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def not-link                ::cause-not-link)
(def inputs-tags-not-vector  ::cause-inputs-tags-not-vector)
(def outputs-tags-not-vector ::cause-outputs-tags-not-vector)
(def handler-not-function    ::cause-handler-not-function)

(def ^:private cause-list [not-link
                           inputs-tags-not-vector
                           outputs-tags-not-vector
                           handler-not-function])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create       [inputs-tags-not-vector
                                                          outputs-tags-not-vector
                                                          handler-not-function]
                                            get-property [not-link]})

;; +---------------------------+
;; |                           |
;; |   EXCEPTION CONSTRUCTOR   |
;; |                           |
;; +---------------------------+

(defn construct
  [type cause message]
  {:pre [(utils/in-list? type-list                          type)
         (utils/in-list? cause-list                         cause)
         (utils/in-list? (types-causes-correspondence type) cause)]}
  (ex-info message {type-keyword  type
                    cause-keyword cause}))
