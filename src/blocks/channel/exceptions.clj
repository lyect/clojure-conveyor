(ns blocks.channel.exceptions
  (:require [utils]))


;; +-----------------------------+
;; |                             |
;; |   EXCEPTION INFO KEYWORDS   |
;; |                             |
;; +-----------------------------+

(def type-keyword  ::channelexceptionkeyword-type-keyword)
(def cause-keyword ::channelexceptionkeyword-cause-keyword)

;; +---------------------+
;; |                     |
;; |   EXCEPTION TYPES   |
;; |                     |
;; +---------------------+

(def define-channel-type  ::channelexceptiontype-define-channel-type)
(def create               ::channelexceptiontype-create)
(def get-channel-property ::channelexceptiontype-get-channel-property)
(def get-channel-field    ::channelexceptiontype-get-channel-field)

(def ^:private type-list [define-channel-type
                          create
                          get-channel-property
                          get-channel-field])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def duplicating-fields         ::channelexceptioncause-duplicating-fields)
(def super-fields-intersection  ::channelexceptioncause-super-fields-intersection)
(def channel-properties-missing ::channelexceptioncause-channel-properties-missing)
(def missing-fields             ::channelexceptioncause-missing-fields)
(def excess-fields              ::channelexceptioncause-excess-fields)
(def type-undeclared            ::channelexceptioncause-type-undeclared)
(def type-defined               ::channelexceptioncause-type-defined)
(def super-undeclared           ::channelexceptioncause-super-undeclared)
(def super-undefined            ::channelexceptioncause-super-undefined)
(def not-channel                ::channelexceptioncause-not-channel)
(def type-undefined             ::channelexceptioncause-type-undefined)
(def unknown-field              ::channelexceptioncause-unknown-field)
(def abstract-creation          ::channelexceptioncause-abstract-creation)

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
                                                                  unknown-field]})

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
