(ns blocks.channel.exceptions
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

(def define-channel-type  ::type-define-channel-type)
(def create               ::type-create)
(def get-channel-property ::type-get-channel-property)
(def get-channel-field    ::type-get-channel-field)
(def abstract             ::type-abstract)


(def ^:private type-list [define-channel-type
                          create
                          get-channel-property
                          get-channel-field
                          abstract])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def duplicating-fields         ::cause-duplicating-fields)
(def super-fields-intersection  ::cause-super-fields-intersection)
(def channel-properties-missing ::cause-channel-properties-missing)
(def missing-fields             ::cause-missing-fields)
(def excess-fields              ::cause-excess-fields)
(def type-undeclared            ::cause-type-undeclared)
(def type-defined               ::cause-type-defined)
(def super-undeclared           ::cause-super-undeclared)
(def super-undefined            ::cause-super-undefined)
(def not-channel                ::cause-not-channel)
(def type-undefined             ::cause-type-undefined)
(def unknown-field              ::cause-unknown-field)
(def abstract-creation          ::cause-abstract-creation)

(def ^:private cause-list [duplicating-fields
                           super-fields-intersection
                           channel-properties-missing
                           missing-fields
                           excess-fields
                           type-undeclared
                           type-defined
                           super-undeclared
                           super-undefined
                           not-channel
                           type-undefined
                           unknown-field
                           abstract-creation])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {define-channel-type [duplicating-fields
                                                                 super-fields-intersection
                                                                 channel-properties-missing
                                                                 type-undeclared
                                                                 type-defined
                                                                 super-undeclared
                                                                 super-undefined]
                                            create              [duplicating-fields
                                                                 missing-fields
                                                                 excess-fields
                                                                 type-undeclared
                                                                 type-undefined
                                                                 abstract-creation]
                                            get-channel-property [not-channel]
                                            get-channel-field    [not-channel
                                                                  unknown-field]
                                            abstract             [type-undeclared]})

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
