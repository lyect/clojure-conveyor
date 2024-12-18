(ns conveyor.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::conveyorexceptionkeyword-type-keyword)
(def cause-keyword ::conveyorexceptionkeyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def create                ::conveyorexceptiontype-create)
(def get-conveyor-property ::conveyorexceptiontype-get-conveyor-property)

(def ^:private type-list [create
                          get-conveyor-property])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def different-input-output      ::conveyorexceptioncause-different-input-output)
(def conveyor-properties-missing ::conveyorexceptioncause-conveyor-properties-missing)
(def not-conveyor                ::conveyorexceptioncause-not-conveyor)

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
                                            get-conveyor-property [not-conveyor]})

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
