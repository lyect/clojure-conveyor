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
(def start                 ::conveyorexceptiontype-start)
(def run                   ::conveyorexceptiontype-run)

(def ^:private type-list [create
                          get-conveyor-property
                          start
                          run])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def different-input-output      ::conveyorexceptioncause-different-input-output)
(def conveyor-properties-missing ::conveyorexceptioncause-conveyor-properties-missing)
(def not-conveyor                ::conveyorexceptioncause-not-conveyor)
(def unknown-channel             ::conveyorexceptioncause-unknown-channel)
(def unknown-vertex              ::conveyorexceptioncause-unknown-vertex)
(def not-all-input-params        ::conveyorexceptioncause-not-all-input-params)

(def ^:private cause-list [different-input-output
                           conveyor-properties-missing
                           not-conveyor
                           unknown-channel
                           unknown-vertex
                           not-all-input-params])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create                [different-input-output
                                                                   conveyor-properties-missing]
                                            get-conveyor-property [not-conveyor]
                                            start                 [not-conveyor
                                                                   not-all-input-params]
                                            run                   [unknown-vertex
                                                                   unknown-channel]})

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
