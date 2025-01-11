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

(def abstract        ::type-abstract)
(def get-property    ::type-get-property)
(def define          ::type-define)
(def get-field-value ::type-get-field-value)
(def get-type-tag    ::type-get-type-tag)
(def create          ::type-create)

(def ^:private type-list [abstract
                          get-property
                          define
                          get-field-value
                          get-type-tag
                          create])

;; +----------------------+
;; |                      |
;; |   EXCEPTION CAUSES   |
;; |                      |
;; +----------------------+

(def type-undeclared                     ::cause-type-undeclared)
(def type-undefined                      ::cause-type-undefined)
(def type-defined                        ::cause-type-defined)
(def super-type-undeclared               ::cause-super-type-undeclared)
(def super-type-undefined                ::cause-super-type-undefined)
(def fields-tags-not-vector              ::cause-fields-tags-not-vector)
(def duplicated-fields-tags              ::cause-duplicated-fields-tags)
(def super-type-fields-tags-intersection ::cause-super-type-fields-tags-intersection)
(def not-channel                         ::cause-not-channel)
(def unknown-field-tag                   ::cause-unknown-field-tag)
(def type-abstract                       ::cause-type-abstract)
(def missed-fields-tags                  ::cause-missed-fields-tags)
(def excess-fields-tags                  ::cause-excess-fields-tags)

(def ^:private cause-list [type-undeclared
                           type-undefined
                           type-defined
                           super-type-undeclared
                           super-type-undefined
                           fields-tags-not-vector
                           duplicated-fields-tags
                           super-type-fields-tags-intersection
                           not-channel
                           unknown-field-tag
                           type-abstract
                           missed-fields-tags
                           excess-fields-tags])

;; +---------------------------------------------------+
;; |                                                   |
;; |   EXCEPTION CAUSES AND TYPES AND CORRESPONDENCE   |
;; |                                                   |
;; +---------------------------------------------------+

(def ^:private types-causes-correspondence {abstract        [type-undeclared
                                                             type-undefined]
                                            get-property    [type-undeclared
                                                             type-undefined]
                                            define          [type-undeclared
                                                             type-defined
                                                             super-type-undeclared
                                                             super-type-undefined
                                                             fields-tags-not-vector
                                                             duplicated-fields-tags
                                                             super-type-fields-tags-intersection]
                                            get-field-value [not-channel
                                                             unknown-field-tag]
                                            get-type-tag    [not-channel]
                                            create          [type-undeclared
                                                             type-undefined
                                                             type-abstract
                                                             duplicated-fields-tags
                                                             missed-fields-tags
                                                             excess-fields-tags]})

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
