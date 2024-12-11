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

(def define-channel-type ::channelexceptiontype-define-channel-type)
(def create              ::channelexceptiontype-create)

(def ^:private type-list [define-channel-type
                          create])

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
(def type-not-declared          ::channelexceptioncause-type-not-declared)
(def type-defined               ::channelexceptioncause-type-defined)

(def ^:private cause-list [duplicating-fields
                           super-fields-intersection
                           channel-properties-missing
                           missing-fields
                           excess-fields
                           type-not-declared
                           type-defined])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {define-channel-type [duplicating-fields
                                                                 super-fields-intersection
                                                                 channel-properties-missing
                                                                 type-not-declared
                                                                 type-defined]
                                            create              [duplicating-fields
                                                                 missing-fields
                                                                 excess-fields]})

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
