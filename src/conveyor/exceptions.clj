(ns conveyor.exceptions
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

(def create                ::type-create)
(def get-conveyor-property ::type-get-conveyor-property)
(def start                 ::type-start)
(def store                 ::type-store)

(def ^:private type-list [create
                          get-conveyor-property
                          start
                          store])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def different-input-output      ::cause-different-input-output)
(def conveyor-properties-missing ::cause-conveyor-properties-missing)
(def not-conveyor                ::cause-not-conveyor)

(def ^:private cause-list [different-input-output
                           conveyor-properties-missing
                           not-conveyor])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create                [different-input-output
                                                                   conveyor-properties-missing]
                                            get-conveyor-property [not-conveyor]
                                            start                 [not-conveyor]
                                            store                 [not-conveyor]})

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
