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

(def create       ::type-create)
(def get-outputs  ::type-get-outputs)
(def start        ::type-start)
(def store        ::type-store)
(def flush-output ::type-flush-output)

(def ^:private type-list [create
                          get-outputs
                          start
                          store
                          flush-output])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def different-input-output      ::cause-different-input-output)
(def conveyor-properties-missing ::cause-conveyor-properties-missing)
(def not-conveyor                ::cause-not-conveyor)
(def not-conveyor-input          ::cause-not-conveyor-input)
(def not-conveyor-output         ::cause-not-conveyor-output)

(def ^:private cause-list [different-input-output
                           conveyor-properties-missing
                           not-conveyor])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {create       [different-input-output
                                                          conveyor-properties-missing]
                                            get-outputs  [not-conveyor]
                                            start        [not-conveyor]
                                            store        [not-conveyor
                                                          not-conveyor-input]
                                            flush-output [not-conveyor
                                                          not-conveyor-output]})

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
